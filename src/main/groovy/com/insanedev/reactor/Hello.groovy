package com.insanedev.reactor

import reactor.core.publisher.Flux

class Hello {
    String say() {
        return 'world'
    }

    Flux flux() {
        return Flux.just("hello", "world")
    }
}
