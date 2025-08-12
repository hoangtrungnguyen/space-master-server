package com.ideaspace.core.repository.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateDocumentRequest(
    val name: String
)