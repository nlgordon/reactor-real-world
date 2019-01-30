package com.insanedev.reactor

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.junit.Test
import reactor.core.publisher.Flux

class TestHotVsCold {
    public static final String TEMP_FILE = "temp.json"
    Gson gson = new Gson()

    JsonReader openFile(String filename) {
        def file = new File(filename)
        JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(file.newInputStream())))
        reader.beginArray()
        println "File opened"
        return reader
    }

    @Test
    void "read json hot"() {
        JsonReader reader = openFile(TEMP_FILE)
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
        Flux<Map> flux = readFile(TEMP_FILE)
        Flux<Map> flux2 = readFile("doesnotexist.json")
        println "Created"

        println "Count: " + flux.count().block()
    }

    Flux<Map> readFile(String filename) {
        Flux<Map> flux = Flux.defer({
            println "Defer called"
            JsonReader reader = openFile(filename)
            Flux.generate({ sink ->
                println "Generate called"
                if (reader.hasNext()) {
                    sink.next(gson.fromJson(reader, Map))
                } else {
                    sink.complete()
                }
            })
        })
        return flux
    }
}
