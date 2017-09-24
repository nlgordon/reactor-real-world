package com.insanedev.reactor

import org.junit.Test
import reactor.core.publisher.Flux

import java.util.stream.Stream

class TestBasicFunctional {

    ArrayList<String> letters = ["a", "b", "c"]
    def toUpper = { letter -> println "Mapped letter: $letter"; letter.toUpperCase() }

    @Test
    void testCStyle() {
        printHeader("C Style")
        for (int i = 0; i < letters.size(); i++) {
            if (letters[i] != "b") {
                println letters[i].toUpperCase()
            }
        }
    }

    @Test
    void testIterator() {
        printHeader("Iterator Style")
        for (letter in letters) {
            if (letter != "b") {
                println letter.toUpperCase()
            }
        }
    }

    @Test
    void testGroovy() {
        printHeader("Groovy Style")
        def groovy = letters
                .findAll({letter -> letter != "b"})
                .collect(toUpper)

        println "Created"

        groovy
                .forEach({upperLetter -> println upperLetter})
    }

    @Test
    void testStream() {
        printHeader("Stream Style")
        Stream stream = letters.stream()
                .filter({letter -> letter != "b"})
                .map(toUpper)

        println "Created"

        stream
                .forEach({upperLetter -> println upperLetter})
    }

    @Test
    void testReactive() {
        printHeader("Reactive Style")

        Flux upperCased = Flux.fromIterable(letters)
                .filter({letter -> letter != "b"})
                .map(toUpper)

        println "Created"

        upperCased
                .subscribe({upperLetter -> println upperLetter})
    }

    void printHeader(String header) {
        println "**** $header ****"
    }
}
