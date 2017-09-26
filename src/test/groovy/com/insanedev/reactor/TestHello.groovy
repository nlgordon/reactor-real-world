package com.insanedev.reactor

import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier

import java.util.concurrent.atomic.AtomicLong

class TestHello {

    @Test
    void "expect hello then world"() {
        Hello hello = new Hello()

        def verifier = StepVerifier.create(hello.flux())

        verifier
                .expectNext("hello")
                .expectNext("world")
                .verifyComplete()
    }

    @Test
    void "lots of output on separate thread"() {
        def diff = new AtomicLong()

        Flux flux = Flux.push({ sink ->
            Flux.range(0, 300).subscribe({
                diff.getAndIncrement()
                sink.next(it)
                sleep(10)
            })
        }, FluxSink.OverflowStrategy.ERROR)
                .publishOn(Schedulers.newSingle("publisher"))

        flux.subscribeOn(Schedulers.immediate()).subscribe({
            def val = diff.getAndDecrement()
            println "$it : $val"
            sleep(1000)
        })
    }
}
