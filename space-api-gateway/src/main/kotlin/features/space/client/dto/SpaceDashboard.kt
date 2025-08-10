package com.space.features.space.client.dto

import kotlinx.serialization.Serializable


@Serializable
data class SpaceDashboardItem(
    val id: String,
    val name: String,
    val revision: Int,
)