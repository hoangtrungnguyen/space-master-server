package com.ideaspace.core.models

import java.time.ZonedDateTime

class AccessToken(
    var signature: String,
    var sessionId: Long,
    var issuedAt: ZonedDateTime,
    var expiresAt: ZonedDateTime,
    var isActive: Boolean
) {
}