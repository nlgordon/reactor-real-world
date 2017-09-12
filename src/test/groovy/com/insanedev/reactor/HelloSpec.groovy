package com.insanedev.reactor

import reactor.test.StepVerifier
import spock.lang.Specification

class HelloSpec extends Specification {

    def "hello world compiles"() {
        setup:
        Hello hello = new Hello()

        when:
        String result = hello.say()

        then:
        result == 'world'
    }

    def "expect hello then world"() {
        setup:
        Hello hello = new Hello()

        when:
        def verifier = StepVerifier.create(hello.flux())

        then:
        verifier
                .expectNext("hello")
                .expectNext("world")
                .verifyComplete()
    }
}
