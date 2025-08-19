package com.ideaspace.core.ram

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class DocumentRAM(
    val id: Long,
    val title: String,
    private val roots: ConcurrentHashMap<UUID, ElementRAM> = ConcurrentHashMap(),
) {

    private val _elements: ConcurrentHashMap<UUID, ElementRAM> = ConcurrentHashMap()
    private val elements get(): MutableMap<UUID, ElementRAM> = _elements

    val size get() = _elements.size
    
    // Public accessor for roots
    fun getRoots(): Map<UUID, ElementRAM> = roots.toMap()

    fun addRoot(element: ElementRAM) {
        roots[element.uuid] = element.copy(path = "${element.uuid}")
        elements[element.uuid] = element.copy(path = "${element.uuid}")
    }

    fun searchElement(uuid: UUID): ElementRAM? {
        if (elements.containsKey(uuid) && elements[uuid]!!.deletedAt == null) {
            val found = elements[uuid]?.also {
                //TODO: if debug, do this check
                findParent(it)
            }
            return found
        }
        return null
    }

    private fun findParent(element: ElementRAM): ElementRAM? {
        if (element.parentUuid == null) {
            val root = roots[element.uuid]
            assert(root != null)
            return null
        } else {
            return elements[element.parentUuid]
        }
    }

    @OptIn(ExperimentalTime::class)
    fun addElement(newElement: ElementRAM): ElementRAM {
        val parentUuid = newElement.parentUuid!!
        val parent = searchElement(parentUuid) ?: throw Exception("Element $parentUuid not found")
        val element = newElement.copy(   path = "${parent.path}/${newElement.uuid}")
        parent.children[element.uuid] = element
        elements[element.uuid] = element
        return element
    }

    fun update(element: ElementRAM) {
        if (element.parentUuid != null) {

            val parent = searchElement(element.parentUuid)!!
            parent.children[element.uuid] = element
            roots[parent.uuid] = parent
            elements[parent.uuid] = parent

            elements[element.uuid] = element
        } else {
            elements[element.uuid] = element
            roots[element.uuid] = element
        }
    }

    fun remove(element: ElementRAM) {
        if (element.parentUuid != null) {
            val parent = searchElement(element.parentUuid)
            parent!!.children.remove(element.uuid)
            elements[parent.uuid] = parent
            elements[element.uuid] = element.copy(deletedAt = Clock.System.now())
        } else {
            elements[element.uuid] = element.copy(deletedAt = Clock.System.now())
        }
    }

}