package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.handler.TitleHandler
import com.disney.studios.titlemanager.repository.TitleRepository
import com.disney.studios.titlemanager.util.TitleLoader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class TitleManagerApplication {

    @Bean
    fun titleLoader(titleRepository: TitleRepository,
                    @Value("\${titles.location}") titlesLocation: String) = TitleLoader(titleRepository, titlesLocation)

    @Bean
    fun titleHandler(titleRepository: TitleRepository) = TitleHandler(titleRepository)

    @Bean
    fun appRoutes(titleHandler: TitleHandler) = router {
        "/titles".nest {
            GET("/", titleHandler::getAllTitles)
            POST("/", titleHandler::createTitle)
            GET("/{id}", titleHandler::getTitleById)
            PUT("/{id}", titleHandler::updateTitle)
            DELETE("/{id}", titleHandler::deleteTitle)
            PUT("/{id}/{child}", titleHandler::addChild)
            DELETE("/{id}/{child}", titleHandler::addChild)
        }
    }

}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .run(*args)
}






