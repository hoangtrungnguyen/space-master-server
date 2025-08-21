package com.ideaspace.core.dto

import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
@Serializable
data class DocumentDashboardItemDTO(
    @Serializable(with = UUIDToString::class)
    val uuid: UUID,
    val title: String,
    @Serializable(with = InstantToISODateTime::class)
    val createdAt: Instant
)