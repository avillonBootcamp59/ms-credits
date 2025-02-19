package proyecto1.mscredit.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import proyecto1.mscredit.client.CustomerClient;
import proyecto1.mscredit.dto.CustomerDTO;
import proyecto1.mscredit.entity.Credit;
import proyecto1.mscredit.repository.CreditRepository;
import proyecto1.mscredit.service.CreditService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return customerClient.getCustomerById(Credit.getCustomerId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado")))
                .flatMap(customer -> creditRepository.findByCustomerId(Credit.getCustomerId()).collectList()
                        .flatMap(existingAccounts -> validateAndCreateCredit( customer, Credit))
                        .flatMap(creditRepository::save));
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
                    return creditRepository.save(existingAccount);
                });
    }

    @Override
    public Flux<Credit> getCreditProductsByCustomer(String id) {
        return creditRepository.findByCustomerId(id)
                .switchIfEmpty(Flux.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontraron créditos para este cliente")));
    }

}