package com.insanedev.reactor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.Statement

// docker run --name reactor-postgres -p 5432:5432/tcp -e POSTGRES_PASSWORD=mysecretpassword -d postgres

@SpringBootTest
class DbSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    def "Expect database records to come back"() {
        setup:
        // drop/create table
        jdbcTemplate.execute("DROP TABLE IF EXISTS words")
        jdbcTemplate.execute("CREATE TABLE words (word VARCHAR)")
        // insert records into db
        jdbcTemplate.update("INSERT INTO words (word) values(?)", "Hello")
        jdbcTemplate.update("INSERT INTO words (word) values(?)", "World")

        when:
        Flux flux = push()

        def verifier = StepVerifier.create(flux)

        then:
        verifier.expectNext("Hello")
                .expectNext("World")
                .verifyComplete()
    }

    Flux generate() {
        Statement statement = dataSource.connection.createStatement()
        ResultSet results = statement.executeQuery("SELECT * FROM words")
        results.next()
        return Flux.generate({ sink ->
            sink.next(results.getString("word"))
            if (!results.next()) {
                sink.complete()
            }
        })
    }

    Flux push() {
        return Flux.push({ sink ->
            Statement statement = dataSource.connection.createStatement()
            ResultSet results = statement.executeQuery("SELECT * FROM words")
            while (results.next()) {
                sink.next(results.getString("word"))
            }
            sink.complete()
        })
    }
}
