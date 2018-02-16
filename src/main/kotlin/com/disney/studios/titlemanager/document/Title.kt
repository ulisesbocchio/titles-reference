package com.disney.studios.titlemanager.document

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.*
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.*
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.*
import org.springframework.core.style.ToStringCreator
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

interface TitleVisitor<out T> {
    fun visit(title: Title): T
    fun visit(title: ChildTitle): T
    fun visit(title: Bonus): T
    fun visit(title: TvSeries): T
    fun visit(title: Season): T
    fun visit(title: Episode): T
}

interface VisitableTitle {
    fun <T> accept(visitor: TitleVisitor<T>): T
}

@Document(collection = "titles")
@JsonTypeInfo(
        use = NAME,
        include = As.PROPERTY,
        property = "type")
@JsonSubTypes(
        Type(value = Bonus::class, name = "Bonus"),
        Type(value = Feature::class, name = "Feature"),
        Type(value = TvSeries::class, name = "TV Series"),
        Type(value = Season::class, name = "Season"),
        Type(value = Episode::class, name = "Episode")
)
abstract class Title(
        @Id
        var id: String? = null,
        var name: String? = null,
        var description: String? = null,
        @DBRef
        var bonuses: List<Bonus>? = null
) : VisitableTitle {
    open fun toStringCreator(): ToStringCreator {
        return ToStringCreator(this)
                .append("id", id)
                .append("name", name)
                .append("description", description)
                .append("bonuses", bonuses)
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Title

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }


}

abstract class ChildTitle(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        bonuses: List<Bonus>? = null
) : Title(id, name, description, bonuses) {
    @Transient
    var parent: Title? = null

    override fun toStringCreator(): ToStringCreator {
        return super.toStringCreator()
                .append("parent", parent?.id);
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }
}

open class Bonus(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        var duration: String? = null
) : ChildTitle(id, name, description, null) {
    override fun toString(): String {
        return toStringCreator()
                .append("duration", duration)
                .toString()
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }
}

class Feature(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        bonuses: List<Bonus>? = null,
        var theatricalReleaseDate: Date? = null,
        var duration: String? = null
) : Title(id, name, description, bonuses) {
    override fun toString(): String {
        return toStringCreator()
                .append("theatricalReleaseDate", theatricalReleaseDate)
                .append("duration", duration)
                .toString()
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }
}

class TvSeries(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        bonuses: List<Bonus>? = null,
        var releaseDate: Date? = null,
        @DBRef
        var seasons: List<Season>? = null
) : Title(id, name, description, bonuses) {
    override fun toString(): String {
        return toStringCreator()
                .append("releaseDate", releaseDate)
                .append("seasons", seasons)
                .toString()
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }
}

class Season(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        bonuses: List<Bonus>? = null,
        var releaseDate: Date? = null,
        @DBRef
        var episodes: List<Episode>? = null
) : ChildTitle(id, name, description, bonuses) {
    override fun toString(): String {
        return toStringCreator()
                .append("releaseDate", releaseDate)
                .append("episodes", episodes)
                .toString()
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }
}

class Episode(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        bonuses: List<Bonus>? = null,
        var releaseDate: Date? = null,
        var duration: String? = null
) : ChildTitle(id, name, description, bonuses) {
    override fun toString(): String {
        return toStringCreator()
                .append("releaseDate", releaseDate)
                .append("duration", duration)
                .toString()
    }

    override fun <T> accept(visitor: TitleVisitor<T>): T {
        return visitor.visit(this)
    }
}