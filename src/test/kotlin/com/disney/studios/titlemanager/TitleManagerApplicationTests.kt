package com.disney.studios.titlemanager

import com.disney.studios.titlemanager.repository.TitleRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.reactive.function.client.WebClient

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [
    TitleManagerApplication::class,
    ReactiveWebServerAutoConfiguration::class,
    HttpHandlerAutoConfiguration::class,
    WebFluxAutoConfiguration::class],
        webEnvironment = RANDOM_PORT)
@DataMongoTest
class TitleManagerApplicationTests {

    @LocalServerPort
    var port: Int? = null

    lateinit var client: WebClient

    @Autowired
    lateinit var titlesRepo: TitleRepository

    @Before
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
	fun contextLoads() {
        println("AAAAAAAAAAAA")
	}

}

