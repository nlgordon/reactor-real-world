package com.insanedev.reactor

import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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

    def "lots of output on separate thread"() {
        setup:
        def diff = new AtomicLong()

        Flux flux = Flux.push({sink ->
            Flux.range(0, 300).subscribe({
                diff.getAndIncrement()
                sink.next(it)
                sleep(10)
            })
        }, FluxSink.OverflowStrategy.ERROR)
                .publishOn(Schedulers.newSingle("publisher"))

        when:
        flux.subscribeOn(Schedulers.immediate()).subscribe({
            def val = diff.getAndDecrement()
            println "$it : $val"
            sleep(1000)
        })

        then:
        1==1
    }
}
