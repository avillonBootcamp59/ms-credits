package com.bank.pe.mscredit.service.impl;

import com.bank.pe.mscredit.dto.CreditDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.bank.pe.mscredit.client.CustomerClient;
import com.bank.pe.mscredit.dto.CustomerDTO;
import com.bank.pe.mscredit.entity.Credit;
import com.bank.pe.mscredit.repository.CreditRepository;
import com.bank.pe.mscredit.service.CreditService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final CustomerClient customerClient; // Feign Client para llamar a ms-customer

    public Mono<Credit> validateAndCreateCredit(CustomerDTO customer, Credit credit) {
        if (customer.getType().equalsIgnoreCase("PERSONAL")) {
            return validatePersonalCredit(credit);
        } else if (customer.getType().equalsIgnoreCase("EMPRESARIAL")) {
            return creditRepository.save(credit);
        } else {
            return Mono.error(new RuntimeException("Tipo de cliente no válido."));
        }
    }

    private Mono<Credit> validatePersonalCredit(Credit credit) {
        return creditRepository.findByCustomerId(credit.getCustomerId())
                .count()
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new RuntimeException("Un cliente personal solo puede tener un crédito."));
                    }
                    return creditRepository.save(credit);
                });
    }

    @Override
    public Flux<Credit> listCredits() {
        return creditRepository.findAll();
    }

    @Override
    public Mono<Credit> getCredit(String id) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crédito no encontrado")));
    }

    @Override
    public Mono<Void> deleteCredit(String id) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crédito no encontrado")))
                .flatMap(creditRepository::delete);
    }

    @Override
    public Mono<Credit> createCredit(Credit Credit) {
        if (Credit == null || Credit.getCustomerId() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos inválidos"));
        }
        return customerClient.getCustomerById(Credit.getCustomerId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")))
                .flatMap(customer -> creditRepository.findByCustomerId(Credit.getCustomerId()).collectList()
                        .flatMap(existingAccounts -> validateAndCreateCredit( customer, Credit))
                      //  .flatMap(creditRepository::save)
                );
    }

    @Override
    public Mono<Credit> updateCredit(String id, Credit updatedCredit) {
        return creditRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cuenta bancaria no encontrada")))
                .flatMap(existingAccount -> {
                    existingAccount.setCreditLimit(updatedCredit.getCreditLimit());
                    existingAccount.setInterestRate(updatedCredit.getInterestRate());
                    existingAccount.setCreditType(updatedCredit.getCreditType());
                    existingAccount.setCurrentDebt(updatedCredit.getCurrentDebt());
                    existingAccount.setAvailableLimit(updatedCredit.getAvailableLimit());
                    existingAccount.setDueDate(updatedCredit.getDueDate());
                    existingAccount.setOutstandingAmount(updatedCredit.getOutstandingAmount());
                    return creditRepository.save(existingAccount);
                });
    }

    @Override
    public Flux<Credit> getCreditProductsByCustomer(String id) {
        return creditRepository.findByCustomerId(id)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontraron créditos para este cliente")));
    }

    @Override
    public Mono<Boolean> hasOverdueDebt(String customerId) {
        return creditRepository.findByCustomerId(customerId)
                .filter(credit -> credit.getDueDate().isBefore(LocalDate.now()) && credit.getOutstandingAmount() > 0)
                .hasElements();
    }

}