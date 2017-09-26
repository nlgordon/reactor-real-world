package com.insanedev.reactor

import reactor.core.publisher.Flux

class Hello {
    Flux flux() {
        return Flux.just("hello", "world")
    }
}
