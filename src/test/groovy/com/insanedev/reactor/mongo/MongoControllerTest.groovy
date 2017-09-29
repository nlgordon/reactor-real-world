package com.insanedev.reactor.mongo

import com.insanedev.reactor.TestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

@RunWith(SpringRunner)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MongoControllerTest {

    @LocalServerPort
    int port

    @Autowired
    WordRepository repository
    @Autowired
    ReactiveMongoTemplate template

    WebTestClient client

    @Before
    void setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
        TestUtils.setupMongo(template, repository)
    }

    @Test
    void testListAllWords() {
        FluxExchangeResult<Word> words = client
                .get()
                .uri("/words/list")
                .exchange() // Groovy fails at type detection here. Seems to be a groovy problem.
                .expectStatus().isOk()
                .returnResult(Word)

        Flux<String> wordStrings = words.responseBody.map({ it.word })
        StepVerifier.create(wordStrings)
                .expectNext("Hello")
                .expectNext("World")
                .verifyComplete()
    }
}
