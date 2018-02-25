package com.disney.studios.titlemanager.document

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.annotation.JsonTypeName
import org.springframework.core.style.ToStringCreator
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

/**
 * Visitor interface for Title, for things such as: update
 */
interface TitleVisitor<out T> {
    fun visit(title: Bonus): T
    fun visit(title: TvSeries): T
    fun visit(title: Season): T
    fun visit(title: Episode): T
    fun visit(title: Feature): T
}

/**
 * Visitable interface to be implemented by concrete Titles to accept visitors
 */
interface VisitableTitle {
    fun <T> accept(visitor: TitleVisitor<T>): T
}

@Document(collection = "titles")
@JsonTypeInfo(use = NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes(Type(Bonus::class), Type(Feature::class), Type(TvSeries::class), Type(Season::class), Type(Episode::class))
@CompoundIndexes(CompoundIndex(def = "{ type: 1 }"))
/**
 * Base Title with common properties
 */
abstract class Title(
        id: String? = null,
        @TextIndexed var name: String? = null,
        var description: String? = null,
        @Transient open var bonuses: List<Bonus>? = null
) : BaseDoc(id), VisitableTitle

abstract class ChildTitle(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        @DBRef var parent: Title? = null
) : Title(id, name, description) {

    override fun propertyToString(name: String, value: Any?): Any? = if (name == "parent") parent?.id else value

}

@TypeAlias("Bonus")
@JsonTypeName("Bonus")
/**
 * Bonus Title
 */
open class Bonus(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        var duration: String? = null
) : ChildTitle(id, name, description) {

    override var bonuses: List<Bonus>?
        @Transient get() = null
        @Transient set(value) {}

    override fun <T> accept(visitor: TitleVisitor<T>): T = visitor.visit(this)

}

@TypeAlias("Feature")
@JsonTypeName("Feature")
/**
 * Feature Title
 */
class Feature(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        var theatricalReleaseDate: Date? = null,
        var duration: String? = null
) : Title(id, name, description) {

    override fun <T> accept(visitor: TitleVisitor<T>): T = visitor.visit(this)

}

@TypeAlias("TV Series")
@JsonTypeName("TV Series")
/**
 * TV Series Title
 */
class TvSeries(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        var releaseDate: Date? = null
) : Title(id, name, description) {

    @Transient
    var seasons: List<Season>? = null

    override fun <T> accept(visitor: TitleVisitor<T>): T = visitor.visit(this)

}

@TypeAlias("Season")
@JsonTypeName("Season")
/**
 * Season Title
 */
class Season(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        var releaseDate: Date? = null
) : ChildTitle(id, name, description) {

    @Transient
    var episodes: List<Episode>? = null

    override fun <T> accept(visitor: TitleVisitor<T>): T = visitor.visit(this)

}

@TypeAlias("Episode")
@JsonTypeName("Episode")
/**
 * Episode Title
 */
class Episode(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        var releaseDate: Date? = null,
        var duration: String? = null
) : ChildTitle(id, name, description) {

    override fun <T> accept(visitor: TitleVisitor<T>): T = visitor.visit(this)

}

/**
 * Base class for Mongo documents with String Id, toString, equals, hashCode, and copy for mutable properties
 */
abstract class BaseDoc(
        @Id var id: String? = null
) {
    override fun toString(): String {
        return this.javaClass.kotlin.memberProperties
                .fold(ToStringCreator(this)) { acc, property ->
                    acc.append(property.name, propertyToString(property.name, property.get(this)))
                }.toString()
    }

    inline fun <reified T : Title> copy(): T {
        return T::class.java.kotlin.memberProperties
                .fold(T::class.createInstance(), { copy, property ->
                    if (property is KMutableProperty<*>) {
                        property.setter.call(copy, property.get(this as T))
                    }
                    copy
                })
    }

    open fun propertyToString(name: String, value: Any?): Any? = value

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
