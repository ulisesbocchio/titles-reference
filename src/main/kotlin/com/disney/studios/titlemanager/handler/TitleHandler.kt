package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.bodyToMono
import com.disney.studios.titlemanager.document.*
import com.disney.studios.titlemanager.json
import com.disney.studios.titlemanager.minus
import com.disney.studios.titlemanager.plus
import com.disney.studios.titlemanager.repository.TitleRepository
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.*
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

class TitleHandler(private val titleRepository: TitleRepository) {

    fun getTitleById(request: ServerRequest) =
            titleRepository.findByIdWithParent(request.pathVariable("id"))
                    .compose { okOrNotFound(it) }

    fun getAllTitles(request: ServerRequest) =
            ServerResponse.ok().json().body(titleRepository.findAllSummaries(*getTypesParam(request)))

    private fun getTypesParam(request: ServerRequest): Array<String> {
        return request
                .queryParam("type")
                .map { it.split(',').toTypedArray() }
                .orElse(emptyArray())
    }


    fun createTitle(request: ServerRequest) = ServerResponse.ok().body(titleRepository.createTitle(request.bodyToMono()))

    fun updateTitle(request: ServerRequest) = titleRepository.findById(request.pathVariable("id"))
            .flatMap { it.accept(TitleUpdater(request.bodyToMono())) }
            .compose { titleRepository.updateTitle(it) }
            .compose { okOrNotFound(it) }

    fun deleteTitle(request: ServerRequest) =
            titleRepository.existsById(request.pathVariable("id"))
                    .flatMap {
                        if (it)
                            titleRepository.deleteById(request.pathVariable("id"))
                                    .compose { ServerResponse.ok().json().body(fromObject(it)) }
                        else ServerResponse.notFound().build()
                    }.compose { okOrNotFound(it) }

    fun addChild(request: ServerRequest) =
            titleRepository.findById(request.pathVariable("id"))
                    .zipWith(titleRepository.findById(request.pathVariable("childId")))
                    .map { addChild(it.t1, it.t2, request.pathVariable("childType")) }
                    .compose { titleRepository.updateTitle(it) }
                    .compose { okOrNotFound(it) }

    fun deleteChild(request: ServerRequest) =
            titleRepository.findById(request.pathVariable("id"))
                    .map { deleteChild(it, request.pathVariable("childId"), request.pathVariable("childType")) }
                    .compose { titleRepository.updateTitle(it) }
                    .compose { okOrNotFound(it) }


    private fun addChild(parent: Title, child: Title, childType: String): Title {
        when (childType) {
            "bonuses" -> doWhen(child, Bonus::class) { parent.bonuses += it }
            "seasons" -> doWhen(parent, TvSeries::class) { series -> doWhen(child, Season::class) { series.seasons += it } }
            "episodes" -> doWhen(parent, Season::class) { season -> doWhen(child, Episode::class) { season.episodes += it } }
            else -> throw HttpClientErrorException(HttpStatus.BAD_REQUEST)
        }
        return parent
    }


    private fun deleteChild(parent: Title, childId: String, childType: String): Title {
        when (childType) {
            "bonuses" -> parent.bonuses -= Bonus(childId)
            "seasons" -> doWhen(parent, TvSeries::class) { it.seasons -= Season(childId) }
            "episodes" -> doWhen(parent, Season::class) { it.episodes -= Episode(childId) }
            else -> throw HttpClientErrorException(HttpStatus.BAD_REQUEST)
        }
        return parent
    }

    private inline fun <reified T : Any> doWhen(title: Title, clazz: KClass<T>, doFn: (T) -> Unit) {
        if (title is T) doFn(title) else HttpClientErrorException(HttpStatus.BAD_REQUEST)
    }

    fun <T : Any> okOrNotFound(title: Mono<T>): Mono<ServerResponse> =
            title.flatMap { ServerResponse.ok().json().body(fromObject(it)) }.switchIfEmpty(ServerResponse.notFound().build())
}