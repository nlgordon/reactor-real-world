package com.insanedev.reactor

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.junit.Test
import reactor.core.publisher.Flux

class TestHotVsCold {
    Gson gson = new Gson()

    JsonReader openFile() {
        def file = new File("temp.json")
        JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(file.newInputStream())))
        reader.beginArray()
        println "File opened"
        return reader
    }

    @Test
    void "read json hot"() {
        JsonReader reader = openFile()
        Flux<Map> flux = Flux.generate({ sink ->
            println "Generate called"
            if (reader.hasNext()) {
                sink.next(gson.<Map>fromJson(reader, Map))
            } else {
                sink.complete()
            }
        })
        println "Created"

        print "Count: " + flux.count().block()
    }

    @Test
    void "read json cold"() {
        Flux<Map> flux = Flux.defer({
            println "Defer called"
            JsonReader reader = openFile()
            Flux.generate({ sink ->
                if (reader.hasNext()) {
                    sink.next(gson.fromJson(reader, Map))
                } else {
                    sink.complete()
                }
            })
        })
        println "Created"

        println "Count: " + flux.count().block()
    }
}
