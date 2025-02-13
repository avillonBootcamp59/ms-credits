package proyecto1.mscredit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proyecto1.mscredit.client.CustomerClient;
import proyecto1.mscredit.dto.CustomerDTO;
import proyecto1.mscredit.entity.Credit;
import proyecto1.mscredit.repository.CreditRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditRepository creditRepository;
    private final CustomerClient customerClient; // Feign Client para llamar a ms-customer

    public Mono<Credit> validateAndCreateCredit(Credit credit) {
        CustomerDTO customer = customerClient.getCustomerById(credit.getCustomerId());
        if (customer == null) {
            return Mono.error(new RuntimeException("Cliente no encontrado."));
        }

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

}