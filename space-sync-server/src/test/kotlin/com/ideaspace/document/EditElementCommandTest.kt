package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditElementEventValue
import com.ideaspace.core.ram.DocumentRAM
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EditElementCommandTest {

    private lateinit var documentRedisPublisher: DocumentRedisPublisher
    private lateinit var elementRepo: ElementRepo
    private lateinit var documentStorage: DocumentStorage
    private lateinit var documentRam: DocumentRAM
    private lateinit var logPublisher: LogPublisher
    private lateinit var documentRepo: CrudDocumentRepository

    private val docId = 1L
    private val processId = 100L

    @BeforeEach
    fun setUp() {
        documentRedisPublisher = mockk(relaxed = true)
        elementRepo = mockk(relaxed = true)
        documentStorage = mockk(relaxed = true)
        documentRam = mockk(relaxed = true)
        logPublisher = mockk(relaxed = true)
        documentRepo = mockk(relaxed = true)

        every { logPublisher.warn(any(),any(), any()) } returns mockk()
        every { documentStorage.documentsMap[docId] } returns documentRam
    }

    @Test
    fun `execute should log a warning if element not found`() = runTest {
        // Given
        val elementUuid = UUID.randomUUID()
        val editDocEventValue = EditElementEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            windowId = 789L,
            uuid = elementUuid,
            metadata = null,
            value = JsonObject(mapOf()),
            type = "shape"
        )

        every { documentRam.searchElement(elementUuid) } returns null

        val command = EditElementCommand(
            editDocEventValue, docId, processId,
            documentRedisPublisher, elementRepo, documentStorage, logPublisher, documentRepo
        )

        // When
        command.execute()

        // Then
        coVerify(exactly = 1) { logPublisher.warn(any(), toLogServer = any()) }
        coVerify(exactly = 0) { documentRam.update(any()) }
        coVerify(exactly = 0) { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) }
        coVerify(exactly = 0) { elementRepo.updateEditedElement(any(), any(), any(), any()) }
        coVerify(exactly = 0) { documentRepo.saveLatestRedisEntry(any(), any()) }
    }

    @Test
    @DisplayName("execute should update an element, publish the event, and update the repo")
    fun `execute should correctly update an element`() = runTest {
        // Given
        val elementUuid = UUID.randomUUID()
        val updatedValue = buildJsonObject { put("text", "new content") }
        val updatedMetadata = buildJsonObject { put("author", "test_user") }
        val updatedType = "text"

        val editDocEventValue = EditElementEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            windowId = 789L,
            uuid = elementUuid,
            value = updatedValue,
            metadata = updatedMetadata,
            type = updatedType
        )

        val originalElement = ElementRAM(
            uuid = elementUuid,
            metadata = null,
            value = JsonNull,
            type = "shape",
            parentUuid = null,
            element = null,
            children = linkedMapOf(),
            path = "",
            deletedAt = null
        )

        every { documentRam.searchElement(elementUuid) } returns originalElement
        coEvery { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) } returns "redis-key-id"
        coEvery { elementRepo.updateEditedElement(any(), any(), any(), any()) } returns mockk()

        val command = EditElementCommand(
            editDocEventValue, docId, processId,
            documentRedisPublisher, elementRepo, documentStorage, logPublisher, documentRepo
        )

        // When
        command.execute()

        // Then
        coVerify { documentRam.update(any()) }
        coVerify { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
        coVerify {
            elementRepo.updateEditedElement(
                uuid = elementUuid,
                metadata = updatedMetadata,
                value = updatedValue,
                type = updatedType
            )
        }
        coVerify {
            documentRepo.saveLatestRedisEntry(docId, "redis-key-id")
        }
    }
}