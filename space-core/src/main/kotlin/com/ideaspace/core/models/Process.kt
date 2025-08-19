@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant


class Process(
    var id: Long,
    var docId: Long,
    var userId: Long,
    var windowId: Long,
    var sessionId: Long,
    var isActive: Boolean,
    var lastActiveAt: Instant
) {
}