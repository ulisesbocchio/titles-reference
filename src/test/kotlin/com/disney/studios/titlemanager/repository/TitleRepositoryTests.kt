package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.TitleManagerApplication
import com.disney.studios.titlemanager.should
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.StepVerifier

@SpringJUnitConfig(classes = [
    TitleManagerApplication::class,
    ReactiveWebServerAutoConfiguration::class,
    HttpHandlerAutoConfiguration::class,
    WebFluxAutoConfiguration::class])
@DataMongoTest
class TitleRepositoryTests {

    @Autowired
    lateinit var titlesRepo: TitleRepository

    @TestFactory
    fun titleRepository() = listOf(
            should("be initialized with 44 titles from titles.json") {
                StepVerifier.create(titlesRepo.findAll())
                        .expectNextCount(44)
                        .verifyComplete()
            }
    )
}