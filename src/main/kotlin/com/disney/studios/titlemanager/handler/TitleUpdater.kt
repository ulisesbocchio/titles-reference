package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.cast
import com.disney.studios.titlemanager.document.*
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
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
                .doOnError { throw HttpClientErrorException(HttpStatus.BAD_REQUEST) }
                .map {
                    title.parent = it.parent ?: title.parent
                    it
                }
    }

    override fun visit(title: Bonus): Mono<Title> {
        return visit(title as ChildTitle)
                .cast<Bonus>()
                .doOnError { throw HttpClientErrorException(HttpStatus.BAD_REQUEST) }
                .map {
                    title.duration = it.duration ?: title.duration
                    title
                }
    }

    override fun visit(title: TvSeries): Mono<Title> {
        return visit(title as Title)
                .cast<TvSeries>()
                .doOnError { throw HttpClientErrorException(HttpStatus.BAD_REQUEST) }
                .map {
                    title.releaseDate = it.releaseDate ?: title.releaseDate
                    title.seasons = it.seasons ?: title.seasons
                    title
                }
    }

    override fun visit(title: Season): Mono<Title> {
        return visit(title as ChildTitle)
                .cast<Season>()
                .doOnError { throw HttpClientErrorException(HttpStatus.BAD_REQUEST) }
                .map {
                    title.releaseDate = it.releaseDate ?: title.releaseDate
                    title.episodes = it.episodes ?: title.episodes
                    title
                }
    }

    override fun visit(title: Episode): Mono<Title> {
        return visit(title as ChildTitle)
                .cast<Episode>()
                .doOnError { throw HttpClientErrorException(HttpStatus.BAD_REQUEST) }
                .map {
                    title.releaseDate = it.releaseDate ?: title.releaseDate
                    title.duration = it.duration ?: title.duration
                    title
                }
    }

}