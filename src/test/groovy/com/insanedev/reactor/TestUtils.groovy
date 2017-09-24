package com.insanedev.reactor

import groovy.json.JsonOutput
import org.junit.Test
import reactor.core.publisher.Flux
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
}
