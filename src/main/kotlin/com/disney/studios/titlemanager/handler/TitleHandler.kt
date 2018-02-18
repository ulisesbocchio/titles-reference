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

class TitleHandler(private val titleRepository: TitleRepository) {

    fun getTitleById(request: ServerRequest) =
            titleRepository.findByIdWithParent(request.pathVariable("id"))
                    .compose { okOrNotFound(it) }

    fun getAllTitles(request: ServerRequest) =
            ServerResponse.ok().body(titleRepository
                    .findAllSummaries(request.queryParam("terms").orElse(null), *request.queryParamValues("type")))

    fun createTitle(request: ServerRequest) = ServerResponse.accepted().body(titleRepository.createTitle(request.bodyToMono()))

    fun updateTitle(request: ServerRequest) = titleRepository.findById(request.pathVariable("id"))
            .flatMap { it.accept(TitleUpdater(request.bodyToMono())) }
            .compose { titleRepository.updateTitle(it) }
            .compose { acceptedOrNotFound(it) }

    fun deleteTitle(request: ServerRequest) =
            titleRepository.existsById(request.pathVariable("id"))
                    .flatMap {
                        if (it)
                            titleRepository.deleteById(request.pathVariable("id"))
                                    .then(ServerResponse.accepted().body(fromObject(it)))
                        else ServerResponse.notFound().build()
                    }

    fun addChild(request: ServerRequest) =
            titleRepository.findById(request.pathVariable("id"))
                    .zipWith(titleRepository.findById(request.pathVariable("childId")))
                    .map { addChild(it.t1, it.t2, request.pathVariable("childType")) }
                    .compose { titleRepository.updateTitle(it) }
                    .compose { acceptedOrNotFound(it) }

    fun deleteChild(request: ServerRequest) =
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