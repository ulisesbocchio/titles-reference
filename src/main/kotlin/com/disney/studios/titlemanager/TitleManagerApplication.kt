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
open class TitleManagerApplication {

    @Bean
    open fun titleLoader(titleRepository: TitleRepository,
                         @Value("\${titles.location}") titlesLocation: String) = TitleLoader(titleRepository, titlesLocation)

    @Bean
    open fun titleHandler(titleRepository: TitleRepository) = TitleHandler(titleRepository)

    @Bean
    open fun appRoutes(titleHandler: TitleHandler) = router {
        "/titles".nest {
            GET("/", titleHandler::getAllTitles)
            POST("/", titleHandler::createTitle)
            GET("/{id}", titleHandler::getTitleById)
            PUT("/{id}", titleHandler::updateTitle)
            DELETE("/{id}", titleHandler::deleteTitle)
            PUT("/{id}/{childType}/{child}", titleHandler::addChild)
            DELETE("/{id}/{childType}/{child}", titleHandler::deleteChild)
        }
    }

}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .run(*args)
}






