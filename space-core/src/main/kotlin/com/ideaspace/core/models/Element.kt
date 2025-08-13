package com.ideaspace.core.models

import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Element(
    var uuid: UUID,
    var docId: Long,
    var parentUuid: UUID?,
    var metadata: JsonElement?,
    var type: String,
    var value: JsonElement,
    var deletedAt: Instant?
)

