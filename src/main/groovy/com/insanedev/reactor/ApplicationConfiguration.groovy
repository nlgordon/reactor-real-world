package com.insanedev.reactor

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication(exclude = [MongoAutoConfiguration, MongoDataAutoConfiguration])
@EnableReactiveMongoRepositories
@AutoConfigureAfter(EmbeddedMongoAutoConfiguration)
class ApplicationConfiguration extends AbstractReactiveMongoConfiguration {

    @Autowired
    Environment environment

    @Bean
    LoggingEventListener mongoEventListener() {
        return new LoggingEventListener()
    }

    @Override
    @Bean
    @DependsOn("embeddedMongoServer")
    MongoClient mongoClient() {
        int port = environment.getProperty("local.mongo.port", Integer.class)
        return MongoClients.create(String.format("mongodb://localhost:%d", port))
    }

    @Override
    protected String getDatabaseName() {
        return "reactive"
    }
}

