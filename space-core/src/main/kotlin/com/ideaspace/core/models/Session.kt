package org.example.com.ideaspace.core.models

import java.time.ZonedDateTime

class Session(
    id: Long,
    userId: Long,
    clientId: String,
    loginMethod: String,
    createdAt: ZonedDateTime,
    isActive: Boolean,
    deviceId: String,
    userAgent: String,
    ipAddress: String,
) {
}