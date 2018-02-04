package com.disney.studios.titlemanager

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [(TitleManagerApplication::class)])
@ContextConfiguration( initializers = [AppBeansHelperInitializer::class])
@DataMongoTest
class TitleManagerApplicationTests {

    @Test
	fun contextLoads() {
        Thread.sleep(5000)
        println("AAAAAAAAAAAA")
	}

}

