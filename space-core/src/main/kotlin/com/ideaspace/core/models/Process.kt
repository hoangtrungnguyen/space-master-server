package com.ideaspace.core.models

import java.time.ZonedDateTime

class Process(
    var id: Long,
    var docId: Long,
    var userId: Long,
    var windowId: Long,
    var sessionId: Long,
    var isActive: Boolean,
    var lastActiveAt: ZonedDateTime
) {
}