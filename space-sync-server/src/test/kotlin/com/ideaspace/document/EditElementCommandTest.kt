package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditElement
import com.ideaspace.core.kafkaMessage.EditElementPayload
import com.ideaspace.core.ram.DocumentRAM
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
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
        val editElement = EditElement(
            uuid = elementUuid,
            value = JsonNull,
            metadata = JsonNull,
            type = "shape"
        )
        val editElementPayload = EditElementPayload(element = editElement)
        val editDocEventValue = EditDocEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            sessionId = 456L,
            clientId = 789L,
            payload = editElementPayload
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

        val editElement = EditElement(
            uuid = elementUuid,
            value = updatedValue,
            metadata = updatedMetadata,
            type = updatedType
        )
        val editElementPayload = EditElementPayload(element = editElement)
        val editDocEventValue = EditDocEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            sessionId = 456L,
            clientId = 789L,
            payload = editElementPayload
        )

        val originalElement = ElementRAM(
            uuid = elementUuid,
            value = JsonNull,
            metadata = JsonNull,
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