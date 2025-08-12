package com.ideaspace.document

import kotlinx.serialization.Serializable

@Serializable
data class DocumentEvent(
    val op: String,
)
