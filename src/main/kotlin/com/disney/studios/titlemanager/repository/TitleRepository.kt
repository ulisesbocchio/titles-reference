package com.disney.studios.titlemanager.repository

import com.disney.studios.titlemanager.document.Title
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface TitleRepository : ReactiveMongoRepository<Title, String>