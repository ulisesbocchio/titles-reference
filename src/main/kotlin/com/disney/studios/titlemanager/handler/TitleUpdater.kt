package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.cast
import com.disney.studios.titlemanager.document.*
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

class TitleUpdater(val update: Mono<Title>) : TitleVisitor<Mono<Title>> {
    override fun visit(title: Title): Mono<Title> {
        return update.map {
            title.description = it.description ?: title.description
            title.name = it.name ?: title.name
            title.bonuses = it.bonuses ?: title.bonuses
            it
        }
    }

    override fun visit(title: ChildTitle): Mono<Title> {
        return visit(title as Title)
                .cast<ChildTitle>()
                .map {
                    title.parent = it.parent ?: title.parent
                    it as Title
                }
                .onErrorMap { badRequest(title) }
    }

    override fun visit(title: Bonus): Mono<Title> {
        return visit(title as ChildTitle)
                .cast<Bonus>()
                .map {
                    title.duration = it.duration ?: title.duration
                    title as Title
                }
                .onErrorMap { badRequest(title) }
    }

    override fun visit(title: TvSeries): Mono<Title> {
        return visit(title as Title)
                .cast<TvSeries>()
                .map {
                    title.releaseDate = it.releaseDate ?: title.releaseDate
                    title.seasons = it.seasons ?: title.seasons
                    title as Title
                }
                .onErrorMap { badRequest(title) }
    }

    override fun visit(title: Season): Mono<Title> {
        return visit(title as ChildTitle)
                .cast<Season>()
                .map {
                    title.releaseDate = it.releaseDate ?: title.releaseDate
                    title.episodes = it.episodes ?: title.episodes
                    title as Title
                }
                .onErrorMap { badRequest(title) }
    }

    override fun visit(title: Episode): Mono<Title> {
        return visit(title as ChildTitle)
                .cast<Episode>()
                .map {
                    title.releaseDate = it.releaseDate ?: title.releaseDate
                    title.duration = it.duration ?: title.duration
                    title as Title
                }
                .onErrorMap { badRequest(title) }
    }

    private fun badRequest(title: Title) =
            ServerWebInputException("Incompatible update type for title type: '${title::class.simpleName}'")

}