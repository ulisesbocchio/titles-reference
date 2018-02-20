package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.handler.TitleHandler
import com.disney.studios.titlemanager.repository.TitleRepository
import com.disney.studios.titlemanager.util.TitleLoader
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
/**
 * Title Manager Spring's configuration class.
 *
 * @property titleLoader is used to bootstrap MongoDB with provided titles.json data.
 *
 * @property titleHandler contains API endpoint handlers.
 *
 * @property mappingMongoConverter is used to override default *_class* attribute in MongoDB docs to use *type* attribute.
 *
 * @property appRoutes contains API request mappings.
 *
 */
class TitleManagerApplication {

    @Bean
    fun titleLoader(titleRepository: TitleRepository,
                    @Value("\${titles.location}") titlesLocation: String) = TitleLoader(titleRepository, titlesLocation)

    @Bean
    fun titleHandler(titleRepository: TitleRepository) = TitleHandler(titleRepository)

    @Bean
    fun mappingMongoConverter(factory: MongoDbFactory,
                              context: MongoMappingContext, beanFactory: BeanFactory,
                              conversions: MongoCustomConversions): MappingMongoConverter {
        val dbRefResolver = DefaultDbRefResolver(factory)
        val mappingConverter = MappingMongoConverter(dbRefResolver,
                context)
        mappingConverter.setCustomConversions(conversions)
        mappingConverter.typeMapper = DefaultMongoTypeMapper("type", context)
        return mappingConverter
    }

    @Bean
    fun appRoutes(titleHandler: TitleHandler) = router {
        "/titles".nest {
            GET("/", titleHandler::getAllTitles)
            POST("/", titleHandler::createTitle)
            GET("/{id}", titleHandler::getTitleById)
            PUT("/{id}", titleHandler::updateTitle)
            DELETE("/{id}", titleHandler::deleteTitle)
            PUT("/{id}/{childType}/{childId}", titleHandler::addChild)
            DELETE("/{id}/{childType}/{childId}", titleHandler::deleteChild)
        }
    }

}

/**
 * Spring Boot app bootstrap
 */
fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .run(*args)
}






