package com.disney.studios.titlemanager

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

fun ServerResponse.BodyBuilder.json() = contentType(MediaType.APPLICATION_JSON_UTF8)

fun <T> Iterable<T>?.toFlux(): Flux<T> = if (this != null) Flux.fromIterable(this) else Flux.empty()

inline fun < reified T : Any> ServerRequest.bodyToMono(): Mono<T> = bodyToMono(T::class.java)