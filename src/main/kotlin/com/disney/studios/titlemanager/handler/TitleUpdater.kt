package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.document.*
import org.springframework.web.server.ServerWebInputException
import kotlin.reflect.KClass

/**
 * Title Visitor that implements the Update operation. It applies a type check to make sure we're not updating
 * one Title type with a different one and operates on populated properties of the [update] Title only.
 */
class TitleUpdater(private val update: Title) : TitleVisitor<Title> {
    private fun Title.updateBaseTitleFields(update: Title) {
        description = update.description ?: description
        name = update.name ?: name
    }

    private fun ChildTitle.updateChildTitleFields(update: ChildTitle) {
        parent = update.parent ?: parent
    }

    override fun visit(title: Bonus): Title = title.update {
        duration = it.duration ?: duration
    }

    override fun visit(title: TvSeries): Title = title.update {
        releaseDate = it.releaseDate ?: releaseDate
    }

    override fun visit(title: Feature): Title = title.update {
        duration = it.duration ?: duration
        theatricalReleaseDate = it.theatricalReleaseDate ?: theatricalReleaseDate
    }

    override fun visit(title: Season): Title = title.update {
        releaseDate = it.releaseDate ?: releaseDate
    }

    override fun visit(title: Episode): Title = title.update {
        releaseDate = it.releaseDate ?: releaseDate
        duration = it.duration ?: duration
    }

    private inline fun <reified T : Title> T.update(crossinline doUpdate: T.(T) -> Unit): Title = apply {
        if (update is T) {
            updateBaseTitleFields(update)
            when (this) {
                is ChildTitle -> updateChildTitleFields(update as ChildTitle)
            }
            doUpdate(update)
        } else {
            throw badRequest()
        }
    }

    private fun Title.badRequest() =
            ServerWebInputException("Incompatible update type: '${update::class.simpleName}' for title type: '${this::class.simpleName}'")
}

/**
 * Title Visitor that establishes parent/child relationship based on parent title, child title and child type. Enforcing
 * that the appropriate parent is being set for a given title.
 */
class ParentSetter(private val parent: Title) : TitleVisitor<Title> {
    override fun visit(title: Bonus): Title = title.setParentWhen { it != Bonus::class }

    override fun visit(title: TvSeries): Title = throw title.invalidRelationship()

    override fun visit(title: Season): Title = title.setParentWhen { it == TvSeries::class }

    override fun visit(title: Episode): Title = title.setParentWhen { it == Season::class }

    override fun visit(title: Feature): Title = throw title.invalidRelationship()

    private fun ChildTitle.setParentWhen(parentValidator: (KClass<*>) -> Boolean): Title = apply {
        if (!parentValidator(this@ParentSetter.parent::class)) {
            throw invalidRelationship()
        }
        parent = this@ParentSetter.parent
    }

    private fun Title.invalidRelationship() =
            ServerWebInputException("Cannot establish relationship between '${parent::class.simpleName}' and '${this::class.simpleName}'")
}

/**
 * Title Visitor that removes a parent from a children based on a parent title id, child title and child type. Enforcing that
 * that parent id is in fact the parent title, and that the appropriate relationship is being removed.
 */
class ParentUnsetter(private val parentId: String) : TitleVisitor<Title> {
    override fun visit(title: Bonus): Title = title.unsetParent()

    override fun visit(title: TvSeries): Title = throw title.invalidRelationship()

    override fun visit(title: Season): Title = title.unsetParent()

    override fun visit(title: Episode): Title = title.unsetParent()

    override fun visit(title: Feature): Title = throw title.invalidRelationship()

    private fun ChildTitle.unsetParent(): Title = apply {
        if (parent?.id != parentId) {
            throw invalidParent(this)
        }
        parent = null
    }

    private fun invalidParent(title: Title) =
            ServerWebInputException("Title $parentId not the parent of ${title.id}")

    private fun Title.invalidRelationship() =
            ServerWebInputException("Invalid child type: '${this::class.simpleName}'")
}