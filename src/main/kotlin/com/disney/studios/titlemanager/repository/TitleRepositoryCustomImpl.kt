package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.ChildTitle
import com.disney.studios.titlemanager.document.Title
import com.disney.studios.titlemanager.find
import com.disney.studios.titlemanager.findById
import com.disney.studios.titlemanager.findOne
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.*
import org.springframework.data.mongodb.core.query.Criteria.where
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 * Spring Data MongoDB Repository extension implementation
 */
class TitleRepositoryCustomImpl(private val mongoOperations: ReactiveMongoOperations) : TitleRepositoryCustom {

    override fun findAllSummaries(terms: String?, vararg types: String): Flux<Title> =
            mongoOperations.find(Query()
                    .types(*types)
                    .matching(terms)
                    .noChildren())

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
        val query = Query().or(
                where("bonuses.\$id").isEqualTo(ObjectId(id)),
                where("episodes.\$id").isEqualTo(ObjectId(id)),
                where("seasons.\$id").isEqualTo(ObjectId(id))
        ).noChildren()
        return mongoOperations.findOne(query)
    }

    private fun Query.or(vararg criteria: Criteria): Query {
        addCriteria(Criteria().orOperator(*criteria))
        return this;
    }

    private fun Query.noChildren(): Query {
        fields()
                .exclude("bonuses")
                .exclude("episodes")
                .exclude("seasons")
        return this
    }

    private fun Query.types(vararg types: String): Query {
        if (types.isNotEmpty()) {
            addCriteria(where("type").inValues(*types))
        }
        return this
    }

    private fun Query.matching(terms: String?): Query {
        if (terms != null) {
            addCriteria(TextCriteria().matching(terms))
        }
        return this
    }
}
