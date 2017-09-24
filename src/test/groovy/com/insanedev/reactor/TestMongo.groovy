package com.insanedev.reactor

import com.insanedev.reactor.mongo.Word
import com.insanedev.reactor.mongo.WordRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RunWith(SpringRunner)
@SpringBootTest
class TestMongo {

    @Autowired
    ReactiveMongoTemplate template
    @Autowired
    WordRepository repository
    @Autowired
    ReactiveMongoOperations operations

    @Before
    void setup() {
        template.collectionExists(Word)
                .flatMap({ exists -> exists ? template.dropCollection(Word) : Mono.just(exists) })
                .flatMap({ exists -> template.createCollection(Word, new CollectionOptions(1024 * 1024, 100, true)) })
                .then()
                .block()

        repository.saveAll(Flux.just(new Word(word: "Hello"), new Word(word: "World")))
                .then()
                .block()

        println "Done with setup"
    }

    @Test
    void "Use repo for query"() {

        Flux<Word> wordObjects = repository.findAll(Sort.by(Order.asc("word")))

        println "Created Flux"

        Flux<String> words = wordObjects.map({ word -> word.word })

        TestUtils.verifyWords(words)
    }

    @Test
    void "Use template for query"() {
        template.find(new Query().with(Sort.by(Order.asc("word"))), Word)

        Flux<Word> wordObjects = template.findAll(Word)

        println "Created Flux"

        Flux<String> words = wordObjects.map({ word -> word.word })

        TestUtils.verifyWords(words)
    }

    @Test
    void "Read in real-time while writing new records"() {
        Flux<Word> words = repository.findWithTailableCursorBy()

        Disposable disposable = words
                .doOnNext({ word -> println "New word found: $word" })
                .doOnComplete({ println "Complete" })
                .subscribe()

        repository.save(new Word(word: "Nate")).subscribe()
        sleep(100)
        repository.save(new Word(word: "is")).subscribe()
        sleep(100)
        repository.save(new Word(word: "having")).subscribe()
        sleep(100)

        disposable.dispose()

        repository.save(new Word(word: "fun")).subscribe()
        sleep(100)
    }
}
