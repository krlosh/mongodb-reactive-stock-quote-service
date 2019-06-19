package guru.springframework.mongodbreactivestockquoteservice.services;

import guru.springframework.mongodbreactivestockquoteservice.client.StockQuoteClient;
import guru.springframework.mongodbreactivestockquoteservice.domain.Quote;
import guru.springframework.mongodbreactivestockquoteservice.repositories.QuoteRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
public class QuoteMonitorService implements ApplicationListener<ContextRefreshedEvent> {

    private final StockQuoteClient stockQuoteClient;
    private final QuoteRepository quoteRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public QuoteMonitorService(StockQuoteClient stockQuoteClient, QuoteRepository quoteRepository, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.stockQuoteClient = stockQuoteClient;
        this.quoteRepository = quoteRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @PostConstruct
    public void init(){
        // Tailable and cap collections
        //  http://claudioed.tech/2018/03/07/continuous-query-with-spring-data-reactive-mongodb/
        //  https://github.com/claudioed/continuous-query-mongodb
        //
        reactiveMongoTemplate.dropCollection("quotes")
                .then(reactiveMongoTemplate.createCollection("quotes",
                        CollectionOptions.empty().capped().size(2048).maxDocuments(10000))).subscribe();
        System.out.println("*************************** POST CONSTRUCTOR");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.stockQuoteClient.getQuoteStream()
                .log("quote-monitor-service")
                .subscribe(quote -> {
                    Mono<Quote> savedQuote = this.quoteRepository.save(quote);
                    System.out.println("I saved a quote, id:"+savedQuote.block().getId());
                });
    }
}
