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

@SpringBootApplication
class TitleManagerApplication {

    @Bean
    fun titleLoader(titleRepository: TitleRepository,
                    @Value("\${titles.location}") titlesLocation: String)= TitleLoader(titleRepository, titlesLocation)

    @Bean
    fun appRoutes(titleRepository: TitleRepository) = router {
        GET("/titles/{id}") {
            ServerResponse.ok().body(titleRepository.findById(it.pathVariable("id")))
        }

        GET("/titles") {
            ServerResponse.ok().body(titleRepository.findAll())
        }
    }

}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .run(*args)
}






