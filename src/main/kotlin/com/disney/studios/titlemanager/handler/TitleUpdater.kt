package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.document.*
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

/**
 * Title Visitor that implements the Update operation. It applies a type check to make sure we're not updating
 * one Title type with a different one and operates on populated properties of the [update] Title only.
 */
class TitleUpdater(private val update: Mono<Title>) : TitleVisitor<Mono<Title>> {
    private fun updateBaseTitleFields(title: Title, update: Title) {
        title.description = update.description ?: title.description
        title.name = update.name ?: title.name
        title.bonuses = update.bonuses ?: title.bonuses
    }

    private fun updateChildTitleFields(title: ChildTitle, update: ChildTitle) {
        title.parent = update.parent ?: title.parent
    }

    override fun visit(title: Bonus): Mono<Title> = updateTitle(title) {
        title.duration = it.duration ?: title.duration
    }

    override fun visit(title: TvSeries): Mono<Title> = updateTitle(title) {
        title.releaseDate = it.releaseDate ?: title.releaseDate
        title.seasons = it.seasons ?: title.seasons
    }

    override fun visit(title: Feature): Mono<Title> = updateTitle(title) {
        title.duration = it.duration ?: title.duration
        title.theatricalReleaseDate = it.theatricalReleaseDate ?: title.theatricalReleaseDate
    }

    override fun visit(title: Season): Mono<Title> = updateTitle(title) {
        title.releaseDate = it.releaseDate ?: title.releaseDate
        title.episodes = it.episodes ?: title.episodes
    }

    override fun visit(title: Episode): Mono<Title> = updateTitle(title) {
        title.releaseDate = it.releaseDate ?: title.releaseDate
        title.duration = it.duration ?: title.duration
    }

    private inline fun <reified T : Title> updateTitle(title: T, crossinline doUpdate: (T) -> Unit): Mono<Title> {
        return update
                .map {
                    if (it is T) {
                        updateBaseTitleFields(title, it)
                        when (title) {
                            is ChildTitle -> updateChildTitleFields(title, it as ChildTitle)
                        }
                        doUpdate(it)
                        title
                    } else {
                        throw badRequest(title)
                    }
                }
    }

    private fun badRequest(title: Title) =
            ServerWebInputException("Incompatible update type: '${update::class.simpleName}' for title type: '${title::class.simpleName}'")

}