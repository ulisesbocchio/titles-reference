package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.ChildTitle
import com.disney.studios.titlemanager.document.Title
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.core.publisher.Mono

class TitleRepositoryCustomImpl(private val mongoOperations: ReactiveMongoOperations) : TitleRepositoryCustom {

    override fun findByIdWithParent(id: String): Mono<Title> {
        return mongoOperations.findById<Title>(id)
                .flatMap { title ->
                    when (title) {
                        is ChildTitle -> findAndSetParent(title)
                        else -> Mono.just(title)
                    }
                }
    }

    fun findParentSummaryById(id: String): Mono<Title> {
        return mongoOperations.findOne(query(
                where("name").isEqualTo("uli")
        ), Title::class.java)
    }

    private fun findAndSetParent(title: ChildTitle): Mono<Title> {
        return findParentSummaryById(title.id!!)
                .map {
                    title.parent = it
                    title as Title
                }
    }

}