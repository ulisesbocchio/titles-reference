package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.TitleManagerApplication
import com.disney.studios.titlemanager.document.Bonus
import com.disney.studios.titlemanager.document.ChildTitle
import com.disney.studios.titlemanager.document.Episode
import com.disney.studios.titlemanager.document.Feature
import com.disney.studios.titlemanager.document.Season
import com.disney.studios.titlemanager.document.TvSeries
import com.disney.studios.titlemanager.should
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import reactor.core.publisher.toMono
import reactor.test.test

@SpringJUnitConfig(classes = [
    TitleManagerApplication::class,
    ReactiveWebServerAutoConfiguration::class,
    HttpHandlerAutoConfiguration::class,
    WebFluxAutoConfiguration::class])
@DataMongoTest
class TitleRepositoryTests {

    @Autowired
    private lateinit var titlesRepo: TitleRepository

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
            titlesRepo.findAllSummaries(types = *arrayOf("Feature"))
                .test()
                .thenConsumeWhile { it is Feature }
                .verifyComplete()
        },

        should("find 3 Feature Titles") {
            titlesRepo.findAllSummaries(types = *arrayOf("Feature"))
                .test()
                .expectNextCount(3)
                .verifyComplete()
        },

        should("find 7 Titles of types: Feature, Season") {
            titlesRepo.findAllSummaries(types = *arrayOf("Feature", "Season"))
                .collectList()
                .test()
                .assertNext {
                    assertThat(it).hasSize(7)
                    assertThat(it).allSatisfy {
                        it is Feature || it is Season
                    }
                }
                .verifyComplete()
        },

        should("find 1 episode by terms") {
            titlesRepo.findAllSummaries(terms = "\"All the Best Cowboys Have Daddy Issues\"")
                .test()
                .assertNext {
                    assertThat(it is Episode).isTrue()
                    assertThat(it.name).isEqualTo("All the Best Cowboys Have Daddy Issues")
                }
                .verifyComplete()
        },

        should("find 1 episode by id") {
            titlesRepo.findAllSummaries(terms = "\"All the Best Cowboys Have Daddy Issues\"")
                .toMono()
                .flatMap { titlesRepo.findByIdWithChildren(it.id!!) }
                .test()
                .assertNext {
                    assertThat(it).matches { it is Episode }
                    assertThat(it.name).isEqualTo("All the Best Cowboys Have Daddy Issues")
                    assertThat((it as ChildTitle).parent).isNotNull()
                    assertThat(it.parent!!.name).isEqualTo("Season 1")
                }
                .verifyComplete()
        },

        should("find 1 Series by id with Seasons") {
            titlesRepo.findAllSummaries(terms = "\"Star Wars: Clone Wars\"")
                .toMono()
                .flatMap { titlesRepo.findByIdWithChildren(it.id!!) }
                .test()
                .assertNext {
                    assertThat(it is TvSeries).isTrue()
                    assertThat(it.name).isEqualTo("Star Wars: Clone Wars")
                    assertThat((it as TvSeries).seasons).isNotNull()
                    assertThat(it.seasons!!).hasSize(2)
                }
                .verifyComplete()
        },

        should("find 1 Season by id with Episodes") {
            titlesRepo.findAllSummaries(terms = "\"Volume 1\"")
                .toMono()
                .flatMap { titlesRepo.findByIdWithChildren(it.id!!) }
                .test()
                .assertNext {
                    assertThat(it is Season).isTrue()
                    assertThat(it.name).isEqualTo("Volume 1")
                    assertThat((it as Season).episodes).isNotNull()
                    assertThat(it.episodes!!).hasSize(20)
                }
                .verifyComplete()
        },

        should(" create and update one title") {
            titlesRepo.createTitle(Bonus(name = "Test Bonus Title").toMono())
                .flatMap { titlesRepo.findById(it.id!!) }
                .compose {
                    titlesRepo.updateTitle(it.map {
                        it.name = "Updated Test Bonus Title"
                        it
                    })
                }
                .flatMap { titlesRepo.findById(it.id!!) }
                .test()
                .assertNext {
                    assertThat(it.name).isEqualTo("Updated Test Bonus Title")
                }
                .verifyComplete()
        },

        should("not find non-existent title") {
            titlesRepo.findById("12345678900987654321abcd")
                .test()
                .verifyComplete()
        },

        should("upsert non-existent title") {
            titlesRepo.updateTitle(Bonus(id = "12345678900987654321abcd").toMono())
                .test()
                .assertNext {
                    assertThat(it.id).isEqualTo("12345678900987654321abcd")
                }
                .verifyComplete()
            titlesRepo.findById("12345678900987654321abcd")
                .test()
                .expectNextCount(1)
                .verifyComplete()
        },

        should(" create and delete one title") {
            titlesRepo.createTitle(Bonus(name = "Test Bonus Title").toMono())
                .flatMap { titlesRepo.findById(it.id!!) }
                .flatMap {
                    titlesRepo.deleteById(it.id!!).then(it.id.toMono())
                }
                .compose { titlesRepo.findById(it) }
                .test()
                .expectComplete()
        }

    )
}