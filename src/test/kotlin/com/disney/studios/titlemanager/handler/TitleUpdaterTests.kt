package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.date
import com.disney.studios.titlemanager.document.*
import com.disney.studios.titlemanager.should
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.toMono
import reactor.test.test

class TitleUpdaterTests {
    @TestFactory
    fun titleUpdater(): Collection<DynamicTest> = listOf(

            should("update Episode with Episode") {
                val bonus = Bonus()
                val title = Episode()
                val update = Episode(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        duration = "100 min",
                        releaseDate = date("2018-02-13"),
                        id = "big no",
                        bonuses = listOf(bonus)
                )
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                            assertThat(title.releaseDate).isEqualTo(update.releaseDate)
                            assertThat(title.duration).isEqualTo(update.duration)
                            assertThat(title.bonuses).containsExactly(bonus)
                        }
                        .verifyComplete()
            },

            should("not update Episode fields not present in update Episode") {
                val bonus = Bonus()
                val update = Episode()
                val title = Episode(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        duration = "100 min",
                        releaseDate = date("2018-02-13"),
                        id = "big no",
                        bonuses = listOf(bonus)
                )
                val titleCopy = title.copy<Episode>()
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isEqualTo(titleCopy.id)
                            assertThat(title.name).isEqualTo(titleCopy.name)
                            assertThat(title.description).isEqualTo(titleCopy.description)
                            assertThat(title.releaseDate).isEqualTo(titleCopy.releaseDate)
                            assertThat(title.duration).isEqualTo(titleCopy.duration)
                            assertThat(title.bonuses).isEqualTo(titleCopy.bonuses)
                        }
                        .verifyComplete()
            },

            should("update Bonus with Bonus") {

                val title = Bonus()
                val update = Bonus(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        duration = "100 min",
                        id = "big no"
                )
                update.bonuses = listOf(Bonus())
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                            assertThat(title.duration).isEqualTo(update.duration)
                            assertThat(title.bonuses).isNull()
                        }
                        .verifyComplete()
            },

            should("not update Bonus fields not present in update Bonus") {

                val update = Bonus()
                val title = Bonus(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        duration = "100 min",
                        id = "big no"
                )
                update.bonuses = listOf(Bonus())
                val titleCopy = title.copy<Bonus>()
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isEqualTo(titleCopy.id)
                            assertThat(title.name).isEqualTo(titleCopy.name)
                            assertThat(title.description).isEqualTo(titleCopy.description)
                            assertThat(title.duration).isEqualTo(titleCopy.duration)
                            assertThat(title.bonuses).isNull()
                        }
                        .verifyComplete()
            },

            should("update Tv Series with Tv Series") {
                val bonus = Bonus()
                val season = Season()
                val title = TvSeries()
                val update = TvSeries(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        releaseDate = date("2018-02-13"),
                        id = "big no",
                        seasons = listOf(season),
                        bonuses = listOf(bonus)
                )
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                            assertThat(title.releaseDate).isEqualTo(update.releaseDate)
                            assertThat(title.seasons).containsExactly(season)
                            assertThat(title.bonuses).containsExactly(bonus)
                        }
                        .verifyComplete()
            },

            should("not update TvSeries fields not present in update TvSeries") {
                val bonus = Bonus()
                val season = Season()
                val update = TvSeries()
                val title = TvSeries(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        releaseDate = date("2018-02-13"),
                        id = "big no",
                        seasons = listOf(season),
                        bonuses = listOf(bonus)
                )
                val titleCopy = title.copy<TvSeries>()
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isEqualTo(titleCopy.id)
                            assertThat(title.name).isEqualTo(titleCopy.name)
                            assertThat(title.description).isEqualTo(titleCopy.description)
                            assertThat(title.releaseDate).isEqualTo(titleCopy.releaseDate)
                            assertThat(title.seasons).isEqualTo(titleCopy.seasons)
                            assertThat(title.bonuses).isEqualTo(titleCopy.bonuses)
                        }
                        .verifyComplete()
            },

            should("update Season with Season") {
                val bonus = Bonus()
                val episode = Episode()
                val title = Season()
                val update = Season(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        releaseDate = date("2018-02-13"),
                        id = "big no",
                        episodes = listOf(episode),
                        bonuses = listOf(bonus)
                )
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                            assertThat(title.releaseDate).isEqualTo(update.releaseDate)
                            assertThat(title.episodes).containsExactly(episode)
                            assertThat(title.bonuses).containsExactly(bonus)
                        }
                        .verifyComplete()
            },

            should("not update Season fields not present in update Season") {
                val bonus = Bonus()
                val episode = Episode()
                val update = Season()
                val title = Season(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        releaseDate = date("2018-02-13"),
                        id = "big no",
                        episodes = listOf(episode),
                        bonuses = listOf(bonus)
                )
                val titleCopy = title.copy<Season>()
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isEqualTo(titleCopy.id)
                            assertThat(title.name).isEqualTo(titleCopy.name)
                            assertThat(title.description).isEqualTo(titleCopy.description)
                            assertThat(title.releaseDate).isEqualTo(titleCopy.releaseDate)
                            assertThat(title.episodes).isEqualTo(titleCopy.episodes)
                            assertThat(title.bonuses).isEqualTo(titleCopy.bonuses)
                        }
                        .verifyComplete()
            },

            should("update Feature with Feature") {
                val bonus = Bonus()
                val title = Feature()
                val update = Feature(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        duration = "100 min",
                        theatricalReleaseDate = date("2018-02-13"),
                        id = "big no",
                        bonuses = listOf(bonus)
                )
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isNull()
                            assertThat(title.name).isEqualTo(update.name)
                            assertThat(title.description).isEqualTo(update.description)
                            assertThat(title.theatricalReleaseDate).isEqualTo(update.theatricalReleaseDate)
                            assertThat(title.bonuses).containsExactly(bonus)
                        }
                        .verifyComplete()
            },

            should("not update Feature feature fields not present in update Feature") {
                val bonus = Bonus()
                val update = Feature()
                val title = Feature(
                        name = "Frozen",
                        description = "Anna and her sister run around freezing things",
                        duration = "100 min",
                        theatricalReleaseDate = date("2018-02-13"),
                        id = "big no",
                        bonuses = listOf(bonus)
                )
                val titleCopy = title.copy<Feature>()
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .assertNext {
                            assertThat(title.id).isEqualTo(titleCopy.id)
                            assertThat(title.name).isEqualTo(titleCopy.name)
                            assertThat(title.description).isEqualTo(titleCopy.description)
                            assertThat(title.theatricalReleaseDate).isEqualTo(titleCopy.theatricalReleaseDate)
                            assertThat(title.bonuses).isEqualTo(titleCopy.bonuses)
                        }
                        .verifyComplete()
            },

            should("fail updating different Title types") {
                val title = Bonus()
                val update = Feature()
                title.accept(TitleUpdater(update.toMono()))
                        .test()
                        .expectError(ServerWebInputException::class.java)
                        .verify()
            }
    )
}