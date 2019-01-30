package com.insanedev.reactor

import groovy.json.JsonOutput
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpContent
import org.junit.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.ipc.netty.http.client.HttpClient
import reactor.ipc.netty.http.client.HttpClientResponse

import java.util.function.Function

class TestHttpClient {
    String URL = "https://api.github.com/users/nlgordon/events"

    @Test
    void "read url"() {
        setup:
        String expectedContent = new URL(URL).query
        HttpClient client = HttpClient.create()

        /*
        HttpClient interaction seems to be around getting chunks of data. If you need to do streaming parsing of
        the entire stream then you need to piece it back together yourself...
        Possibly use some sort of stateful consumer which then selectively sinks to the next step when it makes sense.
        Using response.receiveContent() we can get at the raw bytes, and attempt to read objects. If we fail to read an
        object then we need more data. Add the partial to a stateful area and wait for the next call to subscribe to try
        again.

        Possibly glean some cruft from here: https://spring.io/guides/gs/reactor-thumbnailer/
         */

        Function<HttpClientResponse, Flux<String>> convertResponseToString = { HttpClientResponse response ->
            return response.receiveContent().map({ HttpContent content ->
                ByteBuf buffer = content.content()
                byte[] bytes = new byte[buffer.readableBytes()]
                buffer.readBytes(bytes)
                return new String(bytes)
            })
        }

        when:
        String actualContent = client
                .get(URL)
                .flatMapMany(convertResponseToString)
                .doOnNext({ println "CONTENT: $it"})
                .reduce("", { accumulatedContent, content -> accumulatedContent + content })
                .block()

        println JsonOutput.prettyPrint(actualContent)

        then:
        actualContent == expectedContent

    }

    @Test
    void "webflux client test"() {
        WebClient client = WebClient.create(URL)

        def request = client.get()
        request.accept(MediaType.APPLICATION_JSON)
        Flux<Map> items = request
                .exchange()
                .flatMapMany({ response ->
            response.bodyToFlux(Map)
        })

        items
                .doOnNext({ println "EVENT: $it" })
                .blockLast()

//        items.toStream().forEach({println it})
    }
}
