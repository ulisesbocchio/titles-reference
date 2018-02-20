package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Title
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
/**
 * Spring Data MongoDB Repository interface for [Title].
 */
interface TitleRepository : ReactiveMongoRepository<Title, String>, TitleRepositoryCustom