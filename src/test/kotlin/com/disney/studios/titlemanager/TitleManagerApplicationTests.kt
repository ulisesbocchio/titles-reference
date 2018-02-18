package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.document.*
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
                        .allSatisfy { assertThat(it.id).isNotNull() }
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
            },

            should("404 trying to update non-existent title") {
                client.put()
                        .uri("titles/12345678900987654321abcd")
                        .body(fromObject(Bonus()))
                        .exchange()
                        .expectStatus().isNotFound
            },

            should("404 trying to delete non-existent title") {
                client.delete()
                        .uri("titles/12345678900987654321abcd")
                        .exchange()
                        .expectStatus().isNotFound
            },

            should("find TV Series by Id with seasons") {
                client.get()
                        .uri {
                            it.path("/titles")
                                    .queryParam("type", "TV Series")
                                    .queryParam("terms", "\"Star Wars: Clone Wars\"")
                                    .build()
                        }
                        .exchange()
                        .expectBodyList<TvSeries>()
                        .hasSize(1)
                        .allSatisfy {
                            assertThat(it.name).isEqualTo("Star Wars: Clone Wars")

                            client.get()
                                    .uri("titles/${it.id}")
                                    .exchange()
                                    .expectBody<TvSeries>()
                                    .satisfies {
                                        assertThat(it.seasons)
                                                .hasSize(2)
                                                .allSatisfy {
                                                    client.get()
                                                            .uri("titles/${it.id}")
                                                            .exchange()
                                                            .expectBody<Season>()
                                                            .satisfies {
                                                                assertThat(it.episodes).isNotEmpty
                                                                assertThat(it.parent)
                                                                        .isNotNull()
                                                                        .matches { (it as Title).name == "Star Wars: Clone Wars" }
                                                            }
                                                }
                                    }
                        }
            },

            should("Add and Remove Episode to/from Season") {
                client.get()
                        .uri {
                            it.path("/titles")
                                    .queryParam("terms", "\"Volume 1\"")
                                    .build()
                        }
                        .exchange()
                        .expectBodyList<Season>()
                        .hasSize(1)
                        .allSatisfy { season ->

                            val newEpisode = client.post()
                                    .uri("/titles")
                                    .body(fromObject(Episode(name = "Test Episode Title")))
                                    .exchange()
                                    .expectBody<Episode>()
                                    .returnResult()
                                    .responseBody!!

                            client.put()
                                    .uri("titles/${season.id}/episodes/${newEpisode.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${season.id}")
                                    .exchange()
                                    .expectBody<Season>()
                                    .satisfies {
                                        assertThat(it.episodes).contains(newEpisode)
                                    }

                            client.get()
                                    .uri("titles/${newEpisode.id}")
                                    .exchange()
                                    .expectBody<Episode>()
                                    .satisfies {
                                        assertThat(it.parent).isEqualTo(season)
                                    }

                            client.delete()
                                    .uri("titles/${season.id}/episodes/${newEpisode.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${season.id}")
                                    .exchange()
                                    .expectBody<Season>()
                                    .satisfies {
                                        assertThat(it.episodes).doesNotContain(newEpisode)
                                    }

                            client.get()
                                    .uri("titles/${newEpisode.id}")
                                    .exchange()
                                    .expectBody<Episode>()
                                    .satisfies {
                                        assertThat(it.parent).isNull()
                                    }
                        }
            },

            should("Add and Remove Bonus to/from Title") {
                client.get()
                        .uri {
                            it.path("/titles")
                                    .queryParam("terms", "\"Volume 1\"")
                                    .build()
                        }
                        .exchange()
                        .expectBodyList<Season>()
                        .hasSize(1)
                        .allSatisfy { season ->

                            val newBonus = client.post()
                                    .uri("/titles")
                                    .body(fromObject(Bonus(name = "Test Bonus")))
                                    .exchange()
                                    .expectBody<Bonus>()
                                    .returnResult()
                                    .responseBody!!

                            client.put()
                                    .uri("titles/${season.id}/bonuses/${newBonus.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${season.id}")
                                    .exchange()
                                    .expectBody<Season>()
                                    .satisfies {
                                        assertThat(it.bonuses).contains(newBonus)
                                    }

                            client.get()
                                    .uri("titles/${newBonus.id}")
                                    .exchange()
                                    .expectBody<Bonus>()
                                    .satisfies {
                                        assertThat(it.parent).isEqualTo(season)
                                    }

                            client.delete()
                                    .uri("titles/${season.id}/bonuses/${newBonus.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${season.id}")
                                    .exchange()
                                    .expectBody<Season>()
                                    .satisfies {
                                        assertThat(it.bonuses).doesNotContain(newBonus)
                                    }

                            client.get()
                                    .uri("titles/${newBonus.id}")
                                    .exchange()
                                    .expectBody<Bonus>()
                                    .satisfies {
                                        assertThat(it.parent).isNull()
                                    }
                        }
            },

            should("Add and Remove Season to/from Tv Series") {
                client.get()
                        .uri {
                            it.path("/titles")
                                    .queryParam("terms", "\"Star Wars: Clone Wars\"")
                                    .build()
                        }
                        .exchange()
                        .expectBodyList<TvSeries>()
                        .hasSize(1)
                        .allSatisfy { series ->

                            val newSeason = client.post()
                                    .uri("/titles")
                                    .body(fromObject(Season(name = "Test Season")))
                                    .exchange()
                                    .expectBody<Season>()
                                    .returnResult()
                                    .responseBody!!

                            client.put()
                                    .uri("titles/${series.id}/seasons/${newSeason.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${series.id}")
                                    .exchange()
                                    .expectBody<TvSeries>()
                                    .satisfies {
                                        assertThat(it.seasons).contains(newSeason)
                                    }

                            client.get()
                                    .uri("titles/${newSeason.id}")
                                    .exchange()
                                    .expectBody<Season>()
                                    .satisfies {
                                        assertThat(it.parent).isEqualTo(series)
                                    }

                            client.delete()
                                    .uri("titles/${series.id}/seasons/${newSeason.id}")
                                    .exchange()
                                    .expectStatus().isAccepted

                            client.get()
                                    .uri("titles/${series.id}")
                                    .exchange()
                                    .expectBody<TvSeries>()
                                    .satisfies {
                                        assertThat(it.seasons).doesNotContain(newSeason)
                                    }

                            client.get()
                                    .uri("titles/${newSeason.id}")
                                    .exchange()
                                    .expectBody<Season>()
                                    .satisfies {
                                        assertThat(it.parent).isNull()
                                    }
                        }
            }
    )
}

