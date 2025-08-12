package com.space.com.ideaspace.space.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class SpaceDto(
    val id: String,
    val name: String,
    val revision: Int
)
