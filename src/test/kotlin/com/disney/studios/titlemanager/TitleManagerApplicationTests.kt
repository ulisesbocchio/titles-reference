package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.document.Title
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient

@SpringJUnitConfig(classes = [
    TitleManagerApplication::class,
    ReactiveWebServerAutoConfiguration::class,
    HttpHandlerAutoConfiguration::class,
    WebFluxAutoConfiguration::class])
@SpringBootTest(webEnvironment = RANDOM_PORT)
@DataMongoTest
@TestInstance(PER_CLASS)
class TitleManagerApplicationTests {

    @LocalServerPort
    var port: Int? = null

    lateinit var client: WebTestClient

    @BeforeAll
    fun setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @TestFactory
    fun titleAPI() = listOf(
            should("find all pre-loaded titles") {
                client.get()
                        .uri("/titles")
                        .exchange()
                        .expectBodyList(Title::class.java)
                        .hasSize(44)
            }
    )
}

