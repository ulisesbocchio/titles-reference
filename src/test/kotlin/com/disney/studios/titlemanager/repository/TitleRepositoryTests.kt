package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.TitleManagerApplication
import com.disney.studios.titlemanager.document.ChildTitle
import com.disney.studios.titlemanager.document.Feature
import com.disney.studios.titlemanager.document.Season
import com.disney.studios.titlemanager.document.TvSeries
import com.disney.studios.titlemanager.should
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.test.test

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
                titlesRepo.findAll()
                        .test()
                        .expectNextCount(44)
                        .verifyComplete()
            },

            should("find all summaries which should not contain parent or children") {
                titlesRepo.findAllSummaries()
                        .test()
                        .thenConsumeWhile {
                            var result = it.bonuses == null
                            if (it is ChildTitle) {
                                result = result && it.parent == null
                            }
                            if (it is Season) {
                                result = result && it.episodes == null
                            }
                            if (it is TvSeries) {
                                result = result && it.seasons == null
                            }
                            result
                        }.verifyComplete()
            },
            should("find only Feature") {
                titlesRepo.findAllSummaries("Feature")
                        .test()
                        .thenConsumeWhile { it is Feature }
                        .verifyComplete()
            },

            should("find 3 Feature Titles") {
                titlesRepo.findAllSummaries("Feature")
                        .test()
                        .expectNextCount(3)
                        .verifyComplete()
            },
            should("find 7 Titles of types: Feature, Season") {
                titlesRepo.findAllSummaries("Feature", "Season")
                        .collectList()
                        .test()
                        .assertNext {
                            assertThat(it).hasSize(7)
                            assertThat(it).allSatisfy {
                                it is Feature || it is Season
                            }
                        }
                        .verifyComplete()
            }
    )
}