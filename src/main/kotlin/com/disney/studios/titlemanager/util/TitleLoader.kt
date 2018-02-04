package com.disney.studios.titlemanager.util

import com.disney.studios.titlemanager.document.*
import com.disney.studios.titlemanager.repository.TitleRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bson.types.ObjectId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.DefaultResourceLoader
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux


class TitleLoader(private val titleRepo: TitleRepository, @Value("\${titles.location}") private val titlesLocation: String): ApplicationRunner {

    private val LOG = LoggerFactory.getLogger(TitleLoader::class.java)

    override fun run(args: ApplicationArguments?) {
        LOG.info("LOADING TITLES FROM $titlesLocation")
        val resourceLoader = DefaultResourceLoader()
        val json = jacksonObjectMapper()

        val titlesToImport = json
                .readValue<List<Title>>(resourceLoader.getResource(titlesLocation).inputStream)

        println(titlesToImport)

        val saveTitles: Flux<Title> = titlesToImport
                .toFlux()
                .flatMap {
                    LOG.info("Processing: $it")
                    process(it)
                }
                .flatMap {
                    LOG.info("Saving: $it")
                    titleRepo.save(it)
                    //Mono.just(it)
                }

        titleRepo
                .deleteAll()
                .thenMany(saveTitles)
                .thenMany(titleRepo.findAll())
                .doOnError { LOG.error("Kaboom!", it) }
                .subscribe { println(it) }
    }

    private fun process(title:Title): Publisher<Title> {
        title.id = ObjectId().toHexString()
        return Mono
                .empty<Title>()
                .concatWith(
                    when (title) {
                        is TvSeries-> processChildren(title.seasons, title)
                        is Season -> processChildren(title.episodes, title)
                        else -> Flux.empty()
                    }
                )
                .concatWith(processChildren(title.bonuses, title))
                .concatWith(Mono.just(title))
    }

    private fun processChildren(children: List<ChildTitle>?, parent: Title): Publisher<Title> {
        return children?.toFlux()
                ?.flatMap {
                    it.parent = parent
                    Mono.just(it)
                }
                ?.flatMap { process(it) }
                ?: Mono.empty()
    }
}