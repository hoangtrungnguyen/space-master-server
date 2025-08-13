@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dto

import com.ideaspace.core.models.Element
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class ElementDTO(
    @Serializable(with = UUIDToString::class)
    var uuid: UUID,
    var docId: Long,
    @Serializable(with = UUIDToString::class)
    var parentUuid: UUID?,
    var metadata: JsonElement?,
    var type: String,
    var value: JsonElement,
)

fun Element.toDTO(): ElementDTO = ElementDTO(
    uuid = this.uuid,
    docId = this.docId,
    parentUuid = this.parentUuid,
    metadata = this.metadata,
    type = this.type,
    value = this.value
)