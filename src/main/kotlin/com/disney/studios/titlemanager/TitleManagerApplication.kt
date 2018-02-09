package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.repository.TitleRepository
import com.disney.studios.titlemanager.util.TitleLoader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.toMono

@SpringBootApplication
class TitleManagerApplication {

    @Bean
    fun titleLoader(titleRepository: TitleRepository,
                    @Value("\${titles.location}") titlesLocation: String) = TitleLoader(titleRepository, titlesLocation)

    @Bean
    fun appRoutes(titleRepository: TitleRepository) = router {
        GET("/titles/{id}") {
            titleRepository.findByIdWithParent(it.pathVariable("id"))
                    .flatMap { ServerResponse.ok().json().body(it.toMono()) }
                    .switchIfEmpty(ServerResponse.notFound().build())
        }

        GET("/titles") {
            ServerResponse.ok().json().body(titleRepository.findAllSummaries())
        }
    }

}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .run(*args)
}






