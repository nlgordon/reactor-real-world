package com.insanedev.reactor.mongo

import groovy.transform.Canonical
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.Tailable
import org.springframework.data.repository.reactive.ReactiveSortingRepository
import reactor.core.publisher.Flux

@Document
@Canonical
class Word {
    @Id
    String id
    String word
}

interface WordRepository extends ReactiveSortingRepository<Word, String> {

    @Tailable
    Flux<Word> findWithTailableCursorBy()
}
