package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.bodyToMono
import com.disney.studios.titlemanager.document.*
import com.disney.studios.titlemanager.minus
import com.disney.studios.titlemanager.plus
import com.disney.studios.titlemanager.queryParamValues
import com.disney.studios.titlemanager.repository.TitleRepository
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

/**
 * Handler methods for CRUD Operations on Title, including adding and removing child titles: *bonuses, episodes, seasons*
 */
class TitleHandler(private val titleRepository: TitleRepository) {

    /**
     * Retrieves a Title by its ID. The ID is retrieved from the *id* path variable.
     *
     * @throws ServerWebInputException when title is not found
     */
    fun getTitleById(request: ServerRequest): Mono<ServerResponse> =
            titleRepository.findByIdWithParent(request.pathVariable("id"))
                    .compose { okOrNotFound(it) }

    /**
     * Retrieve all titles, in summary form (without parent or children).
     * *type* and *terms* query params are used to filter results by Title type and words that appear in the
     * [Title.name] property.
     */
    fun getAllTitles(request: ServerRequest): Mono<ServerResponse> =
            ServerResponse.ok().body(titleRepository
                    .findAllSummaries(request.queryParam("terms").orElse(null), *request.queryParamValues("type")))

    /**
     * Creates a Title from the request's *body*.
     */
    fun createTitle(request: ServerRequest): Mono<ServerResponse> = ServerResponse.accepted().body(titleRepository.createTitle(request.bodyToMono()))

    /**
     * Updates a given Title from the request's *body*. The update Title is used as a *delta*, meaning that only non-null properties
     * from the request's body are updated into the existing Title.
     * The ID of the title to update is retrieved from the request's *id* path variable. Any ID contained in the body is ignored.
     *
     * @see TitleUpdater
     * @throws ServerWebInputException when title is not found
     */
    fun updateTitle(request: ServerRequest): Mono<ServerResponse> = titleRepository.findById(request.pathVariable("id"))
            .flatMap { it.accept(TitleUpdater(request.bodyToMono())) }
            .compose { titleRepository.updateTitle(it) }
            .compose { acceptedOrNotFound(it) }

    /**
     * Deletes a Title by ID. The ID is retrieved from the request's *id* path variable.
     *
     * @throws ServerWebInputException when title is not found
     */
    fun deleteTitle(request: ServerRequest): Mono<ServerResponse> =
            titleRepository.existsById(request.pathVariable("id"))
                    .flatMap {
                        if (it)
                            titleRepository.deleteById(request.pathVariable("id"))
                                    .then(ServerResponse.accepted().body(fromObject(it)))
                        else ServerResponse.notFound().build()
                    }

    /**
     * Adds a child to a given title by IDs and child type. The parent ID is retrieved from the request's *id* path variable
     * while the child ID is retrieved from the *childId* path variable. The kind of child is determined by the *childType*
     * path variable (bonus, season, episode).
     *
     * @throws ServerWebInputException when either title is not found or invalid childType
     */
    fun addChild(request: ServerRequest): Mono<ServerResponse> =
            titleRepository.findById(request.pathVariable("id"))
                    .zipWith(titleRepository.findById(request.pathVariable("childId")))
                    .map { addChild(it.t1, it.t2, request.pathVariable("childType")) }
                    .compose { titleRepository.updateTitle(it) }
                    .compose { acceptedOrNotFound(it) }

    /**
     * Removes a child from a given title by IDs and child type. The parent ID is retrieved from the request's *id* path variable
     * while the child ID is retrieved from the *childId* path variable. The kind of child is determined by the *childType*
     * path variable (bonus, season, episode).
     *
     * @throws ServerWebInputException when either title is not found or invalid childType
     */
    fun deleteChild(request: ServerRequest): Mono<ServerResponse> =
            titleRepository.findById(request.pathVariable("id"))
                    .map { deleteChild(it, request.pathVariable("childId"), request.pathVariable("childType")) }
                    .compose { titleRepository.updateTitle(it) }
                    .compose { acceptedOrNotFound(it) }


    private fun addChild(parent: Title, child: Title, childType: String): Title {
        when (childType) {
            "bonuses" -> doWhen(child, Bonus::class) { parent.bonuses += it }
            "seasons" -> doWhen(parent, TvSeries::class) { series -> doWhen(child, Season::class) { series.seasons += it } }
            "episodes" -> doWhen(parent, Season::class) { season -> doWhen(child, Episode::class) { season.episodes += it } }
            else -> throw ServerWebInputException("Invalid child type: $childType")
        }
        return parent
    }


    private fun deleteChild(parent: Title, childId: String, childType: String): Title {
        when (childType) {
            "bonuses" -> parent.bonuses -= Bonus(childId)
            "seasons" -> doWhen(parent, TvSeries::class) { it.seasons -= Season(childId) }
            "episodes" -> doWhen(parent, Season::class) { it.episodes -= Episode(childId) }
            else -> throw ServerWebInputException("Invalid child type: $childType")
        }
        return parent
    }

    private inline fun <reified T : Any> doWhen(title: Title, clazz: KClass<T>, doFn: (T) -> Unit) {
        if (title is T) doFn(title) else throw ServerWebInputException("Incompatible child type: ${clazz.simpleName}")
    }

    fun <T : Any> okOrNotFound(title: Mono<T>): Mono<ServerResponse> =
            title.flatMap { ServerResponse.ok().body(fromObject(it)) }.switchIfEmpty(ServerResponse.notFound().build())

    fun <T : Any> acceptedOrNotFound(title: Mono<T>): Mono<ServerResponse> =
            title.flatMap { ServerResponse.accepted().body(fromObject(it)) }.switchIfEmpty(ServerResponse.notFound().build())
}