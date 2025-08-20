package com.ideaspace.core.ram

import kotlinx.serialization.KSerializer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.time.Clock
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class DocumentRAM(
    val id: Long,
    val title: String,
    private val _roots: ConcurrentHashMap<UUID, ElementRAM> = ConcurrentHashMap(),
    private val _elements: ConcurrentHashMap<UUID, ElementRAM> = ConcurrentHashMap()
) {

    init{
        roots.forEach {
            _elements[it.key] = it.value
        }
    }

    val roots get(): MutableMap<UUID, ElementRAM> = _roots

    val size get() = _elements.size
    
    fun addRoot(element: ElementRAM) {
        _roots[element.uuid] = element
        _roots[element.uuid]!!.path = "${element.uuid}"
        _elements[element.uuid] = element
        _elements[element.uuid]!!.path= "${element.uuid}"
    }

    fun searchElement(uuid: UUID): ElementRAM? {
        if (_elements.containsKey(uuid) && _elements[uuid]!!.deletedAt == null) {
            val found = _elements[uuid]?.also {
                //TODO: if debug, do this check
                findParent(it)
            }
            return found
        }
        return null
    }

    private fun findParent(element: ElementRAM): ElementRAM? {
        if (element.parentUuid == null) {
            val root = _roots[element.uuid]
            assert(root != null)
            return null
        } else {
            return _elements[element.parentUuid]
        }
    }

    @OptIn(ExperimentalTime::class)
    fun addElement(newElement: ElementRAM): ElementRAM {
        val parentUuid = newElement.parentUuid!!
        val parent = searchElement(parentUuid) ?: throw Exception("Element $parentUuid not found")
        newElement.path = "${parent.path}/${newElement.uuid}"
        parent.children[newElement.uuid] = newElement
        _elements[newElement.uuid] = newElement
        return newElement
    }

    fun update(element: ElementRAM) {
        if (element.parentUuid != null) {

            val parent = searchElement(element.parentUuid)!!
            parent.children[element.uuid] = element
            _roots[parent.uuid] = parent
            _elements[parent.uuid] = parent

            _elements[element.uuid] = element
        } else {
            _elements[element.uuid] = element
            _roots[element.uuid] = element
        }
    }

    fun remove(element: ElementRAM) {
        if (element.parentUuid != null) {
            val parent = searchElement(element.parentUuid)
            parent!!.children.remove(element.uuid)
            _elements[parent.uuid] = parent
            _elements[element.uuid]!!.deletedAt = Clock.System.now()
        } else {
            _roots[element.uuid]!!.deletedAt = Clock.System.now()
            _elements[element.uuid]!!.deletedAt = Clock.System.now()
        }
    }

}
