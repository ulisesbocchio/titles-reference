package com.disney.studios.titlemanager

import org.springframework.beans.factory.BeanFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

inline fun <reified T : Any> ReactiveMongoOperations.find(query: Query): Flux<T> = find(query, T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findById(id: String): Mono<T> = findById(id, T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findOne(query: Query): Mono<T> = findOne(query, T::class.java)

inline fun <reified C : Any, reified A : Annotation> findAnnotation() = AnnotationUtils.findAnnotation(C::class.java, A::class.java)

fun ServerResponse.BodyBuilder.json() = contentType(MediaType.APPLICATION_JSON_UTF8)

fun <T> Iterable<T>?.toFlux(): Flux<T> = if (this != null) Flux.fromIterable(this) else Flux.empty()

inline fun < reified T : Any> ServerRequest.bodyToMono(): Mono<T> = bodyToMono(T::class.java)

inline fun <reified T: Any> Mono<*>.cast(): Mono<T> = cast(T::class.java)

inline fun <reified T : Any> BeanFactory.ref(name: String? = null) : T = when (name) {
    null -> getBean(T::class.java)
    else -> getBean(name, T::class.java)
}

operator fun <T> Collection<T>?.plus(element: T): List<T> {
    val result = ArrayList<T>((this?.size ?: 0) + 1)
    result.addAll(this ?: emptyList())
    result.add(element)
    return result
}

operator fun <T> Collection<T>?.minus(element: T): List<T>? {
    val result = ArrayList<T>((this?.size ?: 0) - 1)
    var removed = false
    return this?.filterTo(result) { if (!removed && it == element) { removed = true; false } else true }
}

fun ServerRequest.queryParamValues(name: String): Array<String> {
    return queryParam(name)
            .map { it.split(',').toTypedArray() }
            .orElse(emptyArray())
}

