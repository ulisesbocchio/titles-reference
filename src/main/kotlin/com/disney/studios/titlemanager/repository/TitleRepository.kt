package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Title
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TitleRepository : ReactiveMongoRepository<Title, String>, TitleRepositoryCustom {

    @Query(value = "{}", fields = "{ 'bonuses': 0, 'episodes': 0, 'seasons': 0 }")
    fun findAllSummaries(): Flux<Title>
}