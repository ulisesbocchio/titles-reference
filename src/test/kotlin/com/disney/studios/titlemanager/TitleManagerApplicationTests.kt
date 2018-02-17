package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.document.Bonus
import com.disney.studios.titlemanager.document.Feature
import com.disney.studios.titlemanager.document.Season
import com.disney.studios.titlemanager.document.Title
import org.assertj.core.api.Assertions.assertThat
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
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import org.springframework.web.reactive.function.BodyInserters.fromObject

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
                        .expectBodyList<Title>()
                        .hasSize(44)
            },

            should("find all pre-loaded Feature titles") {
                client.get()
                        .uri { it.path("/titles").queryParam("type", "Feature").build() }
                        .exchange()
                        .expectBodyList<Feature>()
                        .hasSize(3)
                        .consumeWith<ListBodySpec<Feature>> {
                            assertThat(it.responseBody).allSatisfy {
                                assertThat(it.id).isNotNull()
                            }
                        }
            },

            should("find all pre-loaded Feature and Season titles") {
                client.get()
                        .uri { it.path("/titles").queryParam("type", "Feature,Season").build() }
                        .exchange()
                        .expectBodyList<Title>()
                        .hasSize(7)
                        .consumeWith<ListBodySpec<Title>> {
                            assertThat(it.responseBody).allSatisfy {
                                it is Feature || it is Season
                            }
                        }
            },

            should("find Frozen by name restricted by Feature type") {
                client.get()
                        .uri {
                            it.path("/titles")
                                    .queryParam("type", "Feature")
                                    .queryParam("terms", "Frozen")
                                    .build()
                        }
                        .exchange()
                        .expectBodyList<Title>()
                        .hasSize(1)
                        .consumeWith<ListBodySpec<Title>> {
                            assertThat(it.responseBody).allSatisfy {
                                assertThat(it.name).isEqualTo("Frozen")
                            }
                        }
            },

            should("create, update, retrieve and delete one Title") {
                client.post()
                        .uri("/titles")
                        .body(fromObject(Bonus(name = "Test Bonus Title")))
                        .exchange()
                        .expectBody<Bonus>()
                        .satisfies {
                            assertThat(it.id).isNotNull()
                            assertThat(it.name).isEqualTo("Test Bonus Title")

                            it.name = "New Test Bonus Title"
                            client.put()
                                    .uri("titles/${it.id}")
                                    .body(fromObject(it))
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${it.id}")
                                    .exchange()
                                    .expectBody<Bonus>()
                                    .satisfies { assertThat(it.name).isEqualTo("New Test Bonus Title") }

                            client.delete()
                                    .uri("titles/${it.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${it.id}")
                                    .exchange()
                                    .expectStatus().isNotFound

                        }
            }
    )
}

