package com.insanedev.reactor.mongo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class MongoController {

    @Autowired WordRepository repository

    @RequestMapping(path = "/words/list", method = [RequestMethod.GET])
    Flux<Word> list() {
        return repository.findAll()
    }

//    @RequestMapping(path = "/words/{id}", method = [RequestMethod.GET])
//    Mono<Word> getOne(Mono<String> id) {
//        return repository.findById(id)
//    }
}
