package com.ideaspace.core.ram

import com.ideaspace.core.models.Element
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ElementRAM @ExperimentalTime constructor(
    val uuid: UUID,
    val value: JsonElement,
    val metadata: JsonElement,
    val type: String,
    val parentUuid: UUID?,
    val element: ElementRAM?,
    val children: LinkedHashMap<UUID, ElementRAM> = LinkedHashMap(),
    var path: String,
    var deletedAt: Instant?
)

@OptIn(ExperimentalTime::class)
fun Element.toRAM(path: String): ElementRAM {
    return ElementRAM(
        uuid = this.uuid,
        element = null,
        value = this.value,
        metadata = this.metadata ?: JsonObject(emptyMap()),
        path = path,
        children = LinkedHashMap(),
        type = this.type,
        parentUuid = this.parentUuid,
        deletedAt = null,
    )
}