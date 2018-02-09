package com.disney.studios.titlemanager.handler

import com.disney.studios.titlemanager.repository.TitleRepository
import com.disney.studios.titlemanager.json
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class TitleHandler(private val titleRepository: TitleRepository) {

    fun getTitleById(request: ServerRequest) =
        titleRepository.findByIdWithParent(request.pathVariable("id"))
                .flatMap { ServerResponse.ok().json().body(it.toMono()) }
                .switchIfEmpty(ServerResponse.notFound().build())

    fun getAllTitles(request: ServerRequest) =
            ServerResponse.ok().json().body(titleRepository.findAllSummaries())

    fun createTitle(request: ServerRequest) = ServerResponse.ok().body(Mono.empty())

    fun updateTitle(request: ServerRequest) = ServerResponse.ok().body(Mono.empty())

    fun deleteTitle(request: ServerRequest) = ServerResponse.ok().body(Mono.empty())

    fun addChild(request: ServerRequest) = ServerResponse.ok().body(Mono.empty())

    fun deleteChild(request: ServerRequest) = ServerResponse.ok().body(Mono.empty())
}