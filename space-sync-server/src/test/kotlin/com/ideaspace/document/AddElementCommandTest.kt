package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.AddElement
import com.ideaspace.core.kafkaMessage.AddElementPayload
import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.models.Element
import com.ideaspace.core.ram.DocumentRAM
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AddElementCommandTest {
    private lateinit var documentRedisPublisher: DocumentRedisPublisher
    private lateinit var elementRepo: ElementRepo
    private lateinit var documentStorage: DocumentStorage
    private lateinit var documentRam: DocumentRAM
    private lateinit var logPublisher: LogPublisher
    private lateinit var docRepo: CrudDocumentRepository

    private val docId = 1L
    private val processId = 100L

    @BeforeEach
    fun setUp() {
        documentRedisPublisher = mockk()
        elementRepo = mockk()
        documentStorage = mockk()
        documentRam = mockk(relaxed = true)
        logPublisher = mockk(relaxed = true)
        docRepo = mockk()
        // Common setup for all tests
        coEvery { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) } returns "redis-entry-id"
        coEvery { elementRepo.insert(any()) } returns mockk()
        coEvery { docRepo.saveLatestRedisEntry(any(), any()) } just runs

        every { documentStorage.documentsMap } returns mutableMapOf(docId to documentRam)
        every { documentStorage.documentsMap[docId] } returns documentRam
        every { logPublisher.warn(any(),any(), any()) } returns mockk()
    }

    private fun createTestEvent(parentUuid: UUID? = null): EditDocEventValue {
        return EditDocEventValue(
            docId = docId,
            processId = processId,
            userId = 1L,
            sessionId = 1L,
            clientId = 1L,
            payload = AddElementPayload(
                element = AddElement(
                    uuid = UUID.randomUUID(),
                    parentUuid = parentUuid,
                    metadata = JsonNull,
                    type = "shape",
                    value = JsonNull
                )
            )
        )
    }

    @Test
    fun `execute should log a warning and not add element if element uuid already exists`() = runTest {
            // Given
            val existingUuid = UUID.randomUUID()
            val testEvent = createTestEvent(parentUuid = null).copy(
                payload = AddElementPayload(
                    element = AddElement(
                        uuid = existingUuid,
                        parentUuid = null,
                        metadata = JsonNull,
                        type = "shape",
                        value = JsonNull
                    )
                )
            )

        every { documentRam.exist(existingUuid) } returns true

            val command = AddElementCommand(
                testEvent, docId, processId,
                documentRedisPublisher, elementRepo, documentStorage, logPublisher,
                docRepo
            )

            // When
            command.execute()

            // Then
            coVerify(exactly = 1) { logPublisher.warn(any(), any(), any()) }
            coVerify(exactly = 0) { documentRam.addRoot(any()) }
            coVerify(exactly = 0) { documentRam.addElement(any()) }
            coVerify(exactly = 0) { elementRepo.insert(any()) }
            coVerify(exactly = 0) { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) }
            coVerify(exactly = 0) { docRepo.saveLatestRedisEntry(any(), any()) }
        }



    @Nested
    @DisplayName("Root Element Addition")
    inner class RootElementAddition {

        @Test
        fun `execute should add a root element when parentUuid is null`() = runTest {
            // Given
            val testEvent = createTestEvent(parentUuid = null)
            val command = AddElementCommand(
                testEvent, docId, processId,
                documentRedisPublisher, elementRepo, documentStorage, logPublisher,
                docRepo
            )
            val slot = slot<ElementRAM>()

            // When
            command.execute()

            // Then
            coVerify(exactly = 1) { documentRam.addRoot(capture(slot)) }
            coVerify(exactly = 1) { elementRepo.insert(any()) }
            coVerify(exactly = 1) { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
            coVerify(exactly = 1) { docRepo.saveLatestRedisEntry(docId, "redis-entry-id") }
            assertEquals((testEvent.payload as AddElementPayload).element.uuid, slot.captured.uuid)
        }
    }

    @Nested
    @DisplayName("Child Element Addition")
    inner class ChildElementAddition {

        @Test
        fun `execute should add a child element when parentUuid is not null`() = runTest {
            // Given
            val parentUuid = UUID.randomUUID()
            val testEvent = createTestEvent(parentUuid = parentUuid)
            val command = AddElementCommand(
                testEvent, docId, processId,
                documentRedisPublisher, elementRepo, documentStorage, logPublisher, docRepo
            )
            val slot = slot<ElementRAM>()
            val parentElement = ElementRAM(
                uuid = parentUuid,
                value = JsonNull,
                metadata = JsonNull,
                type = "shape",
                parentUuid = null,
                element = null,
                children = LinkedHashMap(),
                path = "",
                deletedAt = null
            )
            every { documentRam.exist(parentUuid) } returns true
            every { documentRam.addElement(any()) } returnsArgument 0
            // When
            command.execute()

            // Then
            coVerify(exactly = 1) { documentRam.addElement(capture(slot)) }
            coVerify(exactly = 1) { elementRepo.insert(any()) }
            coVerify(exactly = 1) { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
            coVerify(exactly = 1) { docRepo.saveLatestRedisEntry(docId, "redis-entry-id") }
            assertEquals((testEvent.payload as AddElementPayload).element.uuid, slot.captured.uuid)
            assertEquals(parentUuid, slot.captured.parentUuid)
        }
    }
}