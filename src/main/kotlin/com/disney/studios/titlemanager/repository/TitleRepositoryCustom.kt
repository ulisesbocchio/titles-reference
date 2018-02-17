package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Title
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TitleRepositoryCustom {
    fun findByIdWithParent(id: String): Mono<Title>
    fun createTitle(title: Mono<Title>): Mono<Title>
    fun updateTitle(title: Mono<Title>): Mono<Title>
    fun findAllSummaries(terms:String? = null, vararg types:String): Flux<Title>
}