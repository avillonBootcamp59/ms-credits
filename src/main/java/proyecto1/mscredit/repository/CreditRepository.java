package proyecto1.mscredit.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import proyecto1.mscredit.entity.Credit;
import reactor.core.publisher.Flux;

@Repository
public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {
    Flux<Credit> findByCustomerId(String customerId);

}

