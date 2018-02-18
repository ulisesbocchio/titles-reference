package com.disney.studios.titlemanager

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.springframework.test.web.reactive.server.WebTestClient.*
import java.text.SimpleDateFormat

fun should(name: String, test: () -> Unit) = DynamicTest.dynamicTest("should $name", test)

fun date(str: String) = SimpleDateFormat("yyyy-MM-dd").parse(str)

inline fun <reified T : Any> ResponseSpec.expectBodyList(): ListBodySpec<T> = expectBodyList(T::class.java)

inline fun <reified T : Any> ResponseSpec.expectBody(): BodySpec<T, *> = expectBody(T::class.java)

fun <T : Any> BodySpec<T, *>.satisfies(consumer: (T) -> Unit) = Assertions.assertThat(returnResult().responseBody!!).satisfies(consumer)

fun <T : Any> ListBodySpec<T>.allSatisfy(consumer: (T) -> Unit) = consumeWith<ListBodySpec<T>> { Assertions.assertThat(it.responseBody!!).allSatisfy(consumer) }