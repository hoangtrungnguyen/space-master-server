package com.space.features.space.models

import kotlinx.serialization.Serializable

@Serializable
data class SpaceModel(
    val id: String,
    val name: String,
    val revision: Int
)