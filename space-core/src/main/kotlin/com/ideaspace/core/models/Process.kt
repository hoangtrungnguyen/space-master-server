@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.models

import com.ideaspace.core.dto.InstantToISODateTime
import com.ideaspace.core.dto.UUIDToString
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class Process(
    var id: Long,
    var docId: Long,
    var userId: Long,
    var windowId: Long,
    var sessionId: Long,
    var isActive: Boolean,
    @Serializable(with = InstantToISODateTime::class)
    var lastActiveAt: Instant,
    @Serializable(with = UUIDToString::class)
    var peerUuid: UUID? = null,
) {
}