package com.space.com.ideaspace.space.models

import kotlinx.serialization.Serializable


enum class OperationType {
    Document,
    Table,
    Shape
}


sealed class Operation {
    abstract val type: OperationType

    @Serializable
    data class DocumentOperation(
        val position: Int
    ) : Operation() {
        override val type: OperationType
            get() = OperationType.Document
    }
}

@Serializable
data class OperationDto(
    val position: Int,
    val type: OperationType
)

