package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.date
import com.disney.studios.titlemanager.document.Bonus
import com.disney.studios.titlemanager.document.Episode
import com.disney.studios.titlemanager.document.Feature
import com.disney.studios.titlemanager.document.Title
import com.disney.studios.titlemanager.should
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.web.client.HttpClientErrorException
import reactor.core.publisher.toMono
import reactor.test.StepVerifier

class TitleUpdaterTests {
    @TestFactory
    fun titleUpdater(): Collection<DynamicTest> = listOf(

            should("update Episode with Episode") {
                val title = Episode()
                val update = Episode()
                update.name = "Frozen"
                update.description = "Anna and her sister run around freezing things"
                update.duration = "100 min"
                update.releaseDate = date("2018-02-13")
                update.id = "big no"
                StepVerifier
                        .create(title.accept(TitleUpdater(update.toMono())))
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                            assertThat(title.releaseDate).isEqualTo(update.releaseDate)
                            assertThat(title.duration).isEqualTo(update.duration)
                        }
                        .verifyComplete()
            },

            should("update Base Title with Bonus") {
                val title = spy<Title>()
                val update = Bonus()
                update.name = "Frozen"
                update.description = "Anna and her sister run around freezing things"
                update.duration = "100 min"
                update.id = "big no"
                StepVerifier
                        .create(title.accept(TitleUpdater(update.toMono())))
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                        }
                        .verifyComplete()
            },

            should("update Base Title with Base Title") {
                val title = spy<Title>()
                val update = mock<Title>()
                update.name = "Frozen"
                update.description = "Anna and her sister run around freezing things"
                update.id = "big no"
                StepVerifier
                        .create(title.accept(TitleUpdater(update.toMono())))
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                        }
                        .verifyComplete()
            },

            should("fail updating Bonus with Feature") {
                val title = Bonus()
                val update = Feature()
                update.name = "Frozen"
                update.description = "Anna and her sister run around freezing things"
                update.duration = "100 min"
                update.id = "big no"
                StepVerifier
                        .create(title.accept(TitleUpdater(update.toMono())))
                        .expectError(HttpClientErrorException::class.java)
            }
    )
}