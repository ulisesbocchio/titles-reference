package com.disney.studios.titlemanager

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.support.beans
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import org.springframework.web.reactive.function.server.*

@SpringBootApplication
class TitleManagerApplication

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .initializers(beans {
                bean {
                    ApplicationRunner {

                        val titleRepo = ref<TitleRepository>()
                        val resourceLoader = DefaultResourceLoader()
                        val json = jacksonObjectMapper()

                        val titlesToImport = json
                                .readValue<List<Title>>(resourceLoader.getResource("classpath:titles.json").inputStream)

                        val saveTitles: Flux<Title> = titlesToImport
                                .toFlux()
                                .flatMap { titleRepo.save(it) }

                        titleRepo
                                .deleteAll()
                                .thenMany(saveTitles)
                                .thenMany(titleRepo.findAll())
                                .subscribe { println(it) }
                    }
                }
                bean {
                    router {
                        val titleRepo = ref<TitleRepository>()
                        GET("/titles/{id}") {
                            ServerResponse.ok().body(titleRepo.findById(it.pathVariable("id")))
                        }

                        GET("/titles") {
                            ServerResponse.ok().body(titleRepo.findAll())
                        }
                    }
                }
            })
            .run(*args)
}

interface TitleRepository : ReactiveMongoRepository<Title, String>

@Document
data class Title(@Id var id: String? = null, var name: String? = null)
