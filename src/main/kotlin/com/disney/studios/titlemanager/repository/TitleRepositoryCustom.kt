package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Title
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Spring Data MongoDB Repository extension Interface for [Title].
 */
interface TitleRepositoryCustom {

    /**
     * Find a Title given an [id] populating parent information if pertinent.
     * To avoid circular dependency errors, the parent is populated in a second step.
     */
    fun findByIdWithChildren(id: String): Mono<Title>

    /**
     * Creates a Title.
     */
    fun createTitle(title: Mono<Title>): Mono<Title>

    /**
     * Updates an existing Title.
     */
    fun updateTitle(title: Mono<Title>): Mono<Title>

    /**
     * Finds all Title Summaries, this is all titles without parent or children
     */
    fun findAllSummaries(terms: String? = null, vararg types: String): Flux<Title>
}