package com.ideaspace.core.models

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Element(
    var uuid: UUID,
    var docId: Long,
    var parentUuid: UUID?,
    var metadata: JsonObject?,
    var type: String,
    var value: JsonObject,
    var deletedAt: Instant?
)

