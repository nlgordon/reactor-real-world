package com.insanedev.reactor

import com.insanedev.reactor.mongo.Word
import com.insanedev.reactor.mongo.WordRepository
import groovy.json.JsonOutput
import org.junit.Test
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class TestUtils {

    @Test
    void "generate json"() {
        def data = (1..10).collect({ it -> [a: it, b: it + 1, c: it * it, d: it / 2] })

        String json = JsonOutput.toJson(data)
        def file = new File("temp.json")
        file.write(json)
    }

    static void verifyWords(Flux<String> flux) {
        def verifier = StepVerifier.create(flux)

        verifier.expectNext("Hello")
                .expectNext("World")
                .verifyComplete()

        println "Verified"
    }

    static void setupMongo(ReactiveMongoTemplate template, WordRepository repository) {
        template.collectionExists(Word)
                .flatMap({ exists -> exists ? template.dropCollection(Word) : Mono.just(exists) })
                .flatMap({ exists -> template.createCollection(Word, new CollectionOptions(1024 * 1024, 100, true)) })
                .then()
                .block()

        repository.saveAll(Flux.just(new Word(word: "Hello"), new Word(word: "World")))
                .then()
                .block()
    }
}
