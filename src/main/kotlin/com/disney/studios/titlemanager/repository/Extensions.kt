package com.disney.studios.titlemanager.repository

import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

inline fun <reified T : Any> ReactiveMongoOperations.find(query: Query): Flux<T> = find(query, T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findById(id: String): Mono<T> = findById(id, T::class.java)

inline fun <reified T : Any> ReactiveMongoOperations.findOne(query: Query): Mono<T> = findOne(query, T::class.java)