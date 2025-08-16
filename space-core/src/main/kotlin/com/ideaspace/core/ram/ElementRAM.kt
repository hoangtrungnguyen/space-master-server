package com.ideaspace.core.ram

import com.ideaspace.core.models.Element
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class DocumentRAM(
    val id: Long,
    val title: String,
    val roots: ConcurrentHashMap<UUID, ElementRAM>,
){

    fun addRoot(uuid: UUID, element: ElementRAM){
        roots[uuid] = element
    }


    fun searchElement(element: ElementRAM, uuid: UUID): ElementRAM?{
        if(element.uuid == uuid){
            return element
        }
        for(child in element.children.values.iterator()){
            val result = searchElement(child, uuid)
            if( result != null){
                return result
            }
        }
        return null
    }


    @OptIn(ExperimentalTime::class)
    fun addElement(root: ElementRAM, parentUuid: UUID, element: ElementRAM): ElementRAM{
        val parent = searchElement(root, parentUuid)
        if(parent != null){
            parent.children[element.uuid] = element
            return element.copy(
                path = "${parent.path}/${element.uuid}"
            )
        } else {
            throw Exception("Element $parentUuid not found")
        }
    }

}

data class ElementRAM @ExperimentalTime constructor(
    val uuid: UUID,
    val value: JsonElement,
    val metadata: JsonElement,
    val element: ElementRAM?,
    val children: LinkedHashMap<UUID, ElementRAM>,
    val path: String,
    val deletedAt: Instant?
)

@OptIn(ExperimentalTime::class)
fun Element.toRAM(): ElementRAM {
    return ElementRAM(
        uuid = this.uuid,
        element = null,
        value = this.value,
        metadata = this.metadata ?: JsonObject(emptyMap()),
        path = this.uuid.toString(),
        children = LinkedHashMap(),
        deletedAt = null,
    )
}