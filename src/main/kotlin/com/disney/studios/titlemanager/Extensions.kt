package com.disney.studios.titlemanager

import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

inline fun <reified T : Any> ReactiveMongoOperations.find(query: Query): Flux<T> = find(query, T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findById(id: String): Mono<T> = findById(id, T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findOne(query: Query): Mono<T> = findOne(query, T::class.java)

fun <T> Iterable<T>?.toFlux(): Flux<T> = if (this != null) Flux.fromIterable(this) else Flux.empty()

inline fun <reified T : Any> ServerRequest.bodyToMono(): Mono<T> = bodyToMono(T::class.java)

inline fun <reified T : Any> Mono<*>.cast(): Mono<T> = cast(T::class.java)

operator fun <T> Collection<T>?.plus(element: T): List<T> {
    val result = ArrayList<T>((this?.size ?: 0) + 1)
    result.addAll(this ?: emptyList())
    result.add(element)
    return result
}

operator fun <T> Collection<T>?.minus(element: T): List<T>? {
    val result = ArrayList<T>((this?.size ?: 0) - 1)
    var removed = false
    return this?.filterTo(result) {
        if (!removed && it == element) {
            removed = true; false
        } else true
    }
}

fun ServerRequest.queryParamValues(name: String): Array<String> {
    return queryParam(name)
            .map { it.split(',').toTypedArray() }
            .orElse(emptyArray())
}

