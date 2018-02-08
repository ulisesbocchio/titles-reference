package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Title
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TitleRepository : ReactiveMongoRepository<Title, String>, TitleRepositoryCustom {

    @Query(value = "{}", fields = "{ 'bonuses': 0, 'episodes': 0, 'seasons': 0 }")
    fun findAllSummaries(): Flux<Title>

//    @Query(value = "{ \$or: [{ 'bonuses.\$id': ?0 }, { 'episodes.\$id': ?0 }, { 'seasons.\$id': ?0 }] }", fields = "{ 'bonuses': 0, 'episodes': 0, 'seasons': 0 }")
//    fun findParentSummaryById(id: String): Mono<Title>
}