package com.disney.studios.titlemanager

import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * [ReactiveMongoOperations.find] operation with reified type to avoid explicit type argument.
 *
 * @see ReactiveMongoOperations.find
 */
inline fun <reified T : Any> ReactiveMongoOperations.find(query: Query): Flux<T> = find(query, T::class.java)

/**
 * [ReactiveMongoOperations.findById] operation with reified type to avoid explicit type argument.
 *
 * @see ReactiveMongoOperations.findById
 */
inline fun <reified T : Any> ReactiveMongoOperations.findById(id: String): Mono<T> = findById(id, T::class.java)

/**
 * [ReactiveMongoOperations.findOne] operation with reified type to avoid explicit type argument.
 *
 * @see ReactiveMongoOperations.findOne
 */
inline fun <reified T : Any> ReactiveMongoOperations.findOne(query: Query): Mono<T> = findOne(query, T::class.java)

/**
 * Converts a nullable [Iterable] to [Flux].
 *
 * @see Flux.fromIterable
 */
fun <T> Iterable<T>?.toFlux(): Flux<T> = if (this != null) Flux.fromIterable(this) else Flux.empty()


/**
 * Converts a [ServerRequest] body into a [Mono].
 *
 * @see ServerRequest.bodyToMono
 */
inline fun <reified T : Any> ServerRequest.bodyToMono(): Mono<T> = bodyToMono(T::class.java)

/**
 * Converts a [ServerRequest] body into a [Mono] and then applies a [Mono.block] to retrieve its value.
 *
 * @see ServerRequest.bodyToMono
 */
inline fun <reified T : Any> ServerRequest.bodyToObject(): T = bodyToMono<T>().block()!!

/**
 * [Mono.cast] with reified type to avoid explicit type argument.
 *
 * @see [Mono.cast]
 */
inline fun <reified T : Any> Mono<*>.cast(): Mono<T> = cast(T::class.java)

/**
 * [Collection.plus] operator for nullable collections. If collection is null, it returns a new list with the given [element].
 *
 * @see [Collection.plus]
 */
operator fun <T> Collection<T>?.plus(element: T): List<T> {
    val result = ArrayList<T>((this?.size ?: 0) + 1)
    result.addAll(this ?: emptyList())
    result.add(element)
    return result
}

/**
 * [Collection.minus] operator for nullable collections. If collection is null, it returns null.
 *
 * @see [Collection.minus]
 */
operator fun <T> Collection<T>?.minus(element: T): List<T>? {
    if (this != null) {
        val result = ArrayList<T>(this.size  - 1)
        var removed = false
        return this.filterTo(result) {
            if (!removed && it == element) {
                removed = true; false
            } else true
        }
    }
    return null
}

/**
 * Returns a query param from [ServerRequest.queryParam] and splits it by *','* (comma).
 *
 * @see ServerRequest.queryParam
 */
fun ServerRequest.queryParamValues(name: String): Array<String> {
    return queryParam(name)
            .map { it.split(',').toTypedArray() }
            .orElse(emptyArray())
}

