package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.document.Title
import com.disney.studios.titlemanager.repository.TitleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier

@SpringJUnitConfig(classes = [
    TitleManagerApplication::class,
    ReactiveWebServerAutoConfiguration::class,
    HttpHandlerAutoConfiguration::class,
    WebFluxAutoConfiguration::class])
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DataMongoTest
class TitleManagerApplicationTests {

    @LocalServerPort
    var port: Int? = null

    lateinit var client: WebTestClient

    @Autowired
    lateinit var titlesRepo: TitleRepository

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @Test
    fun shouldFindAllLoadedTitlesFromRepository() {
        StepVerifier.create(titlesRepo.findAll())
                .expectNextCount(44)
                .verifyComplete()
    }

    @Test
    fun shouldFindAllLoadedTitlesFromApi() {
        client.
                get()
                .uri("/titles")
                .exchange()
                .expectBodyList(Title::class.java)
                .hasSize(44)
    }

}

