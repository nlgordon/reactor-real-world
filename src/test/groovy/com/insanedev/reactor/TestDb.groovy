package com.insanedev.reactor

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

@RunWith(SpringRunner)
@SpringBootTest
class TestDb {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    @Before
    void setup() {
        // drop/create table
        jdbcTemplate.execute("DROP TABLE IF EXISTS words")
        jdbcTemplate.execute("CREATE TABLE words (id INT, word VARCHAR)")
        // insert records into db
        jdbcTemplate.update("INSERT INTO words (id, word) values(?, ?)", 1, "Hello")
        jdbcTemplate.update("INSERT INTO words (id, word) values(?, ?)", 2, "World")
    }

    @Test
    void "Flux via push"() {
        Flux flux = Flux.push({ sink ->
            ResultSet results = queryWords()

            while (results.next()) {
                sink.next(results.getString("word"))
            }
            sink.complete()
        })

        println "Created"

        TestUtils.verifyWords(flux)
    }

    @Test
    void "Flux via generate"() {
        ResultSet results = queryWords()
        results.next()

        Flux flux = Flux.generate({ sink ->
            sink.next(results.getString("word"))
            if (!results.next()) {
                sink.complete()
            }
        })

        println "Created"

        TestUtils.verifyWords(flux)
    }

    @Test
    void "Flux via create"() {
        Flux flux = Flux.create({ sink ->
            jdbcTemplate.query("SELECT * FROM words", new RowCallbackHandler() {

                @Override
                void processRow(ResultSet rs) throws SQLException {
                    sink.next(rs.getString("word"))
                }
            })
            sink.complete()
            println "Queried DB"
        })

        println "Created"

        TestUtils.verifyWords(flux)
    }

    ResultSet queryWords() {
        Statement statement = dataSource.connection.createStatement()
        ResultSet results = statement.executeQuery("SELECT * FROM words")
        println "Queried DB"
        return results
    }
}
