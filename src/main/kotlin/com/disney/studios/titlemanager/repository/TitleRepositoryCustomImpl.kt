package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Bonus
import com.disney.studios.titlemanager.document.Episode
import com.disney.studios.titlemanager.document.Feature
import com.disney.studios.titlemanager.document.Season
import com.disney.studios.titlemanager.document.Title
import com.disney.studios.titlemanager.document.TitleVisitor
import com.disney.studios.titlemanager.document.TvSeries
import com.disney.studios.titlemanager.find
import com.disney.studios.titlemanager.findById
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Spring Data MongoDB Repository extension implementation
 */
class TitleRepositoryCustomImpl(private val mongoOperations: ReactiveMongoOperations) : TitleRepositoryCustom {

    override fun findAllSummaries(terms: String?, vararg types: String): Flux<Title> =
        mongoOperations.find(Query()
            .types(*types)
            .matching(terms)
            .noParent())

    override fun createTitle(title: Mono<Title>): Mono<Title> = mongoOperations.insert(title)

    override fun updateTitle(title: Mono<Title>): Mono<Title> = mongoOperations.save(title)

    // Spring data mongodb doesn't handle circular references gracefully so manual intervention is needed here.
    // Basically instead of dealing with children as a DBRefs, we fabricate them with transient properties
    override fun findByIdWithChildren(id: String): Mono<Title> =
        mongoOperations.findById<Title>(id)
            .populateChildren()

    private fun Mono<Title>.populateChildren(): Mono<Title> =
        zipWhen {
            mongoOperations.find<Title>(Query()
                .byParent(it.id!!)
                .noParent())
                .collectList()
        }.map { it.t1.accept(ChildrenPopulator(it.t2)) }

    private fun Query.noParent(): Query {
        fields()
            .exclude("parent")
        return this
    }

    private fun Query.byParent(parentId: String): Query {
        addCriteria(where("parent.\$id").isEqualTo(ObjectId(parentId)))
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

/**
 * Visitor that populates children for a given Title
 */
private class ChildrenPopulator(private val children: List<Title>) : TitleVisitor<Title> {
    override fun visit(title: Bonus): Title = title

    override fun visit(title: TvSeries): Title = title.apply {
        with(children) {
            seasons = filterIsInstance<Season>()
            bonuses = filterIsInstance<Bonus>()
        }
    }

    override fun visit(title: Season): Title = title.apply {
        with(children) {
            episodes = filterIsInstance<Episode>()
            bonuses = filterIsInstance<Bonus>()
        }
    }

    override fun visit(title: Episode): Title = title.apply {
        with(children) {
            bonuses = filterIsInstance<Bonus>()
        }
    }

    override fun visit(title: Feature): Title = title.apply {
        with(children) {
            bonuses = filterIsInstance<Bonus>()
        }
    }
}
