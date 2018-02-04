package com.disney.studios.titlemanager.document

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.core.style.ToStringCreator
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = Bonus::class, name = "Bonus"),
        JsonSubTypes.Type(value = Feature::class, name = "Feature"),
        JsonSubTypes.Type(value = TvSeries::class, name = "TV Series"),
        JsonSubTypes.Type(value = Season::class, name = "Season"),
        JsonSubTypes.Type(value = Episode::class, name = "Episode")
)
abstract class Title(
        @Id
        var id: String? = null,
        var name: String? = null,
        var description: String?,
        @DBRef
        var bonuses: List<Bonus>? = emptyList()
) {
    open fun toStringCreator(): ToStringCreator {
        return ToStringCreator(this)
                .append("id", id)
                .append("name", name)
                .append("description", description)
                .append("bonuses", bonuses)
    }
}

abstract class ChildTitle(
        id: String? = null,
        name: String? = null,
        description: String?,
        bonuses: List<Bonus>?,
        @DBRef
        var parent: Title?
) : Title(id, name, description, bonuses) {
    override fun toStringCreator(): ToStringCreator {
        return super.toStringCreator()
                .append("parent", parent?.id);
    }
}

class Bonus(
        id: String? = null,
        name: String? = null,
        description: String?,
        parent: Title?,
        var duration: String
) : ChildTitle(id, name, description, null, parent) {
    override fun toString(): String {
        return toStringCreator()
                .append("duration", duration)
                .toString()
    }
}

class Feature(
        id: String? = null,
        name: String? = null,
        description: String?,
        bonuses: List<Bonus>?,
        var theatricalReleaseDate: Date,
        var duration: String
) : Title(id, name, description, bonuses) {
    override fun toString(): String {
        return toStringCreator()
                .append("theatricalReleaseDate", theatricalReleaseDate)
                .append("duration", duration)
                .toString()
    }
}

class TvSeries(
        id: String? = null,
        name: String? = null,
        description: String?,
        bonuses: List<Bonus>?,
        var releaseDate: Date,
        @DBRef
        var seasons: List<Season>? = emptyList()
) : Title(id, name, description, bonuses) {
    override fun toString(): String {
        return toStringCreator()
                .append("releaseDate", releaseDate)
                .append("seasons", seasons)
                .toString()
    }
}

class Season(
        id: String? = null,
        name: String? = null,
        description: String?,
        bonuses: List<Bonus>?,
        parent: TvSeries?,
        var releaseDate: Date,
        @DBRef
        var episodes: List<Episode>? = emptyList()
) : ChildTitle(id, name, description, bonuses, parent) {
    override fun toString(): String {
        return toStringCreator()
                .append("releaseDate", releaseDate)
                .append("episodes", episodes)
                .toString()
    }
}

class Episode(
        id: String? = null,
        name: String? = null,
        description: String?,
        bonuses: List<Bonus>?,
        parent: Season?,
        var releaseDate: Date,
        var duration: String
) : ChildTitle(id, name, description, bonuses, parent) {
    override fun toString(): String {
        return toStringCreator()
                .append("releaseDate", releaseDate)
                .append("duration", duration)
                .toString()
    }
}