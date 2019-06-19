package guru.springframework.mongodbreactivestockquoteservice.repositories;

import guru.springframework.mongodbreactivestockquoteservice.domain.Quote;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface QuoteRepository extends ReactiveMongoRepository<Quote,String> {

    @Tailable
    Flux<Quote> findWithTailableCursorBy();
}
