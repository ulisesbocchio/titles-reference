package com.disney.studios.titlemanager.util

import com.disney.studios.titlemanager.toFlux
import com.disney.studios.titlemanager.document.ChildTitle
import com.disney.studios.titlemanager.document.Season
import com.disney.studios.titlemanager.document.Title
import com.disney.studios.titlemanager.document.TvSeries
import com.disney.studios.titlemanager.repository.TitleRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.bson.types.ObjectId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.DefaultResourceLoader
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 * [ApplicationRunner] that clears all titles from MongoDB, inserts all titles from provided JSON file, and prints them
 * all out to the application's log.
 */
class TitleLoader(private val titleRepo: TitleRepository, private val titlesLocation: String) : ApplicationRunner {

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
                }

        titleRepo
                .deleteAll()
                .thenMany(saveTitles)
                .thenMany(titleRepo.findAll())
                .doOnError { throw it }
                .log()
                .blockLast()
    }

    private fun process(title: Title): Publisher<Title> {
        title.id = ObjectId().toHexString()
        return Mono
                .empty<Title>()
                .concatWith(
                        when (title) {
                            is TvSeries -> processChildren(title.seasons, title)
                            is Season -> processChildren(title.episodes, title)
                            else -> Flux.empty()
                        }
                )
                .concatWith(processChildren(title.bonuses, title))
                .concatWith(title.toMono())
    }

    private fun processChildren(children: List<ChildTitle>?, parent: Title): Publisher<Title> {
        return children.toFlux()
                .map {
                    it.parent = parent
                    it
                }
                .flatMap { process(it) }
    }
}