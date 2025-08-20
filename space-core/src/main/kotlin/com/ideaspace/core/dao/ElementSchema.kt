@file:OptIn(ExperimentalTime::class)

package com.ideaspace.core.dao

import com.ideaspace.com.ideaspace.core.dao.CurrentTimestamp
import com.ideaspace.core.models.Element
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.datetime.time
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import java.util.*
import kotlin.time.ExperimentalTime


object ElementTable: UUIDTable("elements", "uuid") {

    val docId = long("doc_id").references(DocumentTable.id)
    val parentUuid = uuid("parent_uuid").references(id, onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE).nullable()
    val metadata = jsonb<JsonElement>("metadata", Json, JsonElement.serializer()).nullable()
    val type = varchar("type", 255)
    val value = jsonb<JsonElement>("value", Json, JsonElement.serializer())
    val deletedAt = timestamp("deleted_at").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

class ElementDAO(uuid: EntityID<UUID>) : UUIDEntity(id = uuid) {
    companion object : UUIDEntityClass<ElementDAO>(ElementTable)

    var uuid by ElementTable.id
    var docId by ElementTable.docId
    var parentUuid by ElementTable.parentUuid
    var metadata by ElementTable.metadata
    var type by ElementTable.type
    var value by ElementTable.value
    var deletedAt by ElementTable.deletedAt
}

fun ElementDAO.toEntity(): Element {
    return Element(
        uuid = this.uuid.value,
        docId = this.docId,
        parentUuid = this.parentUuid,
        metadata = this.metadata,
        type = this.type,
        value = this.value,
        deletedAt = this.deletedAt
    )
}