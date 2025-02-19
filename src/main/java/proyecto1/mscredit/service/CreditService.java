package proyecto1.mscredit.service;

import proyecto1.mscredit.entity.Credit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {

    public Flux<Credit> listCredits();
    public Mono<Credit> getCredit(String id);
    public Mono<Void> deleteCredit(String id);
    public Mono<Credit> createCredit(Credit Credit);
    public Mono<Credit> updateCredit(String id, Credit updatedCredit);
    public Flux<Credit> getCreditProductsByCustomer(String id);

}