package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.ChildTitle
import com.disney.studios.titlemanager.document.Title
import com.disney.studios.titlemanager.findById
import com.disney.studios.titlemanager.findOne
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class TitleRepositoryCustomImpl(private val mongoOperations: ReactiveMongoOperations) : TitleRepositoryCustom {

    override fun createTitle(title: Mono<Title>): Mono<Title> = mongoOperations.insert(title)

    override fun updateTitle(title: Mono<Title>): Mono<Title> = mongoOperations.save(title)

    // Spring data mongodb doesn't handle circular references gracefully so manual intervention is needed here.
    // Basically instead of dealing with the parent as a DBRef, we fabricate it into a transient property of  ChildTitle
    override fun findByIdWithParent(id: String): Mono<Title> =
            mongoOperations.findById<Title>(id)
                    .flatMap { title ->
                        when (title) {
                            is ChildTitle -> findAndSetParent(title)
                            else -> title.toMono()
                        }
                    }

    private fun findAndSetParent(title: ChildTitle): Mono<Title> =
            findParentSummaryById(title.id!!)
                    .map {
                        title.parent = it
                        title as Title
                    }.defaultIfEmpty(title)

    private fun findParentSummaryById(id: String): Mono<Title> {
        val query = query(
                Criteria().orOperator(
                        where("bonuses.\$id").isEqualTo(ObjectId(id)),
                        where("episodes.\$id").isEqualTo(ObjectId(id)),
                        where("seasons.\$id").isEqualTo(ObjectId(id))
                )
        )
        query.fields()
                .exclude("bonuses")
                .exclude("episodes")
                .exclude("seasons")
        return mongoOperations.findOne(query)
    }
}