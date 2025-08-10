package com.space.features.operation

import kotlinx.serialization.Serializable


@Serializable
data class ServerOperation(
    val operationId: String,
    val boardId: String,
    val userId: String,
    val revision: Int,
)

class Operation {

}