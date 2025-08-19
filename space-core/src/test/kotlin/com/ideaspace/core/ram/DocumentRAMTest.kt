package com.ideaspace.core.ram

import com.ideaspace.core.models.Element
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.test.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DocumentRAMTest {

    private fun createTestElement(parentUuid: UUID? = null): ElementRAM {
        return ElementRAM(
            uuid = UUID.randomUUID(),
            value = JsonNull,
            metadata = JsonNull,
            type = "shape",
            parentUuid = parentUuid,
            element = null,
            children = LinkedHashMap(),
            path = "",
            deletedAt = null
        )
    }

    @Test
    fun `addRoot should add an element to roots and elements maps`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()

        // When
        document.addRoot(rootElement)

        // Then
        assertEquals(1, document.size)
        assertNotNull(document.searchElement(rootElement.uuid))
        assertEquals("${rootElement.uuid}", document.searchElement(rootElement.uuid)?.path)
    }

    @Test
    fun `addElement should add a child to a parent element`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()
        document.addRoot(rootElement)

        val childElement = createTestElement(parentUuid = rootElement.uuid)

        // When
        val addedChild = document.addElement(childElement)

        // Then
        assertEquals(2, document.size)
        val parent = document.searchElement(rootElement.uuid)
        assertNotNull(parent)
        assertEquals(1, parent.children.size)
        val childInParent = parent.children[childElement.uuid]
        assertNotNull(childInParent)
        assertEquals(childElement.uuid, childInParent.uuid)
        assertNotNull(document.searchElement(childElement.uuid))
        assertEquals("${parent.path}/${childElement.uuid}", addedChild.path)
    }

    @Test
    fun `addElement should add a child to a lv2 parent element`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()
        document.addRoot(rootElement)

        val lv1Element = createTestElement(parentUuid = rootElement.uuid)

        // When
        val addedLv1Element = document.addElement( lv1Element)

        val lv2Element = createTestElement(parentUuid = addedLv1Element.uuid)
        val addedLv2Element = document.addElement(lv2Element)


        // Then
        assertEquals(3, document.size)
        val parent = document.searchElement(rootElement.uuid)
        assertNotNull(parent)
        assertEquals(1, parent.children.size)

        val lv2Parent = document.searchElement(addedLv1Element.uuid)
        assertNotNull(lv2Parent)
        assertEquals(1, lv2Parent.children.size)
        assertEquals("${parent.path}/${lv2Parent.uuid}", addedLv1Element.path)


        val lv3Child = document.searchElement(lv2Element.uuid)
        assertNotNull(lv3Child)
        assertEquals("${parent.uuid}/${lv2Parent.uuid}/${lv3Child.uuid}", addedLv2Element.path)
    }

    @Test
    fun `addElement should throw exception if parent does not exist`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val nonExistentParentUuid = UUID.randomUUID()
        val childElement = createTestElement(parentUuid = nonExistentParentUuid)

        // When & Then
        assertFailsWith<Exception>("Element $nonExistentParentUuid not found") {
            document.addElement(childElement)
        }
    }



    @Test
    fun `searchElement should return null for a deleted element`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()
        document.addRoot( rootElement)
        document.remove(rootElement)

        // When
        val found = document.searchElement(rootElement.uuid)

        // Then
        assertNull(found)
    }

    @Test
    fun `update should modify properties of an element`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()
        document.addRoot(rootElement)

        val updatedElement = rootElement.copy(type = "updated-type")

        // When
        document.update(updatedElement)

        // Then
        val found = document.searchElement(rootElement.uuid)
        assertNotNull(found)
        assertEquals("updated-type", found.type)
    }

    @Test
    fun `remove should mark a root element as deleted`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()
        document.addRoot( rootElement)

        // When
        document.remove(rootElement)

        // Then
        assertNull(document.searchElement(rootElement.uuid))
        assertEquals(1, document.size) // Element is still in the map, just marked as deleted
    }

    @Test
    fun `remove should detach a child element and mark it as deleted`() {
        // Given
        val document = DocumentRAM(id = 1, title = "Test Document")
        val rootElement = createTestElement()
        document.addRoot( rootElement)
        val childElement = createTestElement(parentUuid = rootElement.uuid)
        document.addElement( childElement)

        // When
        document.remove(childElement)

        // Then
        val parent = document.searchElement(rootElement.uuid)
        assertNotNull(parent)
        assertTrue(parent.children.isEmpty())
        assertNull(document.searchElement(childElement.uuid))
        assertEquals(2, document.size)
    }

    @Test
    fun `toRAM should convert an Element to an ElementRAM`() {
        // Given
        val element = Element(
            uuid = UUID.randomUUID(),
            docId = 1L,
            parentUuid = UUID.randomUUID(),
            metadata = JsonObject(emptyMap()),
            type = "text",
            value = JsonNull,
            deletedAt = null
        )
        val path = "root/${element.uuid}"

        // When
        val elementRAM = element.toRAM(path)

        // Then
        assertEquals(element.uuid, elementRAM.uuid)
        assertEquals(element.parentUuid, elementRAM.parentUuid)
        assertEquals(element.type, elementRAM.type)
        assertEquals(path, elementRAM.path)
        assertTrue(elementRAM.children.isEmpty())
        assertNull(elementRAM.deletedAt)
    }
}
