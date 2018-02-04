package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.repository.TitleRepository
import com.disney.studios.titlemanager.util.TitleLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class TitleManagerApplication {

}

fun main(args: Array<String>) {
    SpringApplicationBuilder()
            .sources(TitleManagerApplication::class.java)
            .initializers(appBeans())
            .run(*args)
}

fun appBeans() = beans {
    bean<TitleLoader>()
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
}






