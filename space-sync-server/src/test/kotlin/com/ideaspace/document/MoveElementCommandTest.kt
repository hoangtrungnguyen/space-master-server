package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.MoveElement
import com.ideaspace.core.kafkaMessage.MoveElementPayload
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MoveElementCommandTest {

    private lateinit var documentRedisPublisher: DocumentRedisPublisher
    private lateinit var elementRepo: ElementRepo
    private lateinit var documentStorage: DocumentStorage
    private lateinit var documentRam: DocumentRAM
    private lateinit var logPublisher: LogPublisher
    private lateinit var docRepository: CrudDocumentRepository

    private val docId = 1L
    private val processId = 100L

    @BeforeEach
    fun setUp() {
        documentRedisPublisher = mockk(relaxed = true)
        elementRepo = mockk(relaxed = true)
        documentStorage = mockk(relaxed = true)
        documentRam = mockk(relaxed = true)
        logPublisher = mockk(relaxed = true)
        docRepository = mockk(relaxed = true)

        coEvery { logPublisher.warn(any(), any(), any()) } just runs
        every { documentStorage.documentsMap[docId] } returns documentRam
    }

    @Test
    fun `execute should log a warning if element not found`() = runTest {
        // Given
        val elementUuid = UUID.randomUUID()
        val moveElement = MoveElement(uuid = elementUuid, parentUuid = null)
        val moveElementPayload = MoveElementPayload(element = moveElement)
        val editDocEventValue = EditDocEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            windowId = 789L,
            payload = moveElementPayload
        )

        every { documentRam.searchElement(elementUuid) } returns null

        val command = MoveElementCommand(
            editDocEventValue, docId, processId,
            documentRedisPublisher, elementRepo, documentStorage, logPublisher,
            documentRepository = docRepository
        )

        // When
        command.execute()

        // Then
        coVerify(exactly = 1) { logPublisher.warn(any(), any(), any()) }
        coVerify(exactly = 0) { documentRam.remove(any()) }
        coVerify(exactly = 0) { documentRam.addRoot(any()) }
        coVerify(exactly = 0) { documentRam.addElement(any()) }
        coVerify(exactly = 0) { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) }
        coVerify(exactly = 0) { elementRepo.updateMovedElement(any(), any()) }
        coVerify(exactly = 0) { docRepository.saveLatestRedisEntry(any(), any()) }
    }


    @Nested
    @DisplayName("When parentUuid is not null")
    inner class WithParent {
        @Test
        @DisplayName("execute should move an element, publish the event, and update the repo")
        fun `execute should correctly move an element`() = runTest {
            // Given
            val elementUuid = UUID.randomUUID()
            val newParentUuid = UUID.randomUUID()

            val moveElement = MoveElement(uuid = elementUuid, parentUuid = newParentUuid)
            val moveElementPayload = MoveElementPayload(element = moveElement)
            val editDocEventValue = EditDocEventValue(
                docId = docId,
                processId = processId,
                userId = 123L,
                windowId = 789L,
                payload = moveElementPayload
            )

            val elementToMove = ElementRAM(
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

            every { documentRam.searchElement(elementUuid) } returns elementToMove
            coEvery { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) } returns "redis-entry-id"
            coEvery { elementRepo.updateMovedElement(any(), any()) } returns 1

            val command = MoveElementCommand(
                editDocEventValue, docId, processId,
                documentRedisPublisher, elementRepo, documentStorage, logPublisher,
                documentRepository = docRepository
            )

            // When
            command.execute()

            // Then
            coVerify { documentRam.remove(elementToMove) }
            coVerify { documentRam.addElement(any()) }
            coVerify { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
            coVerify {
                elementRepo.updateMovedElement(
                    uuid = elementUuid,
                    parentUuid = newParentUuid
                )
            }
            coVerify { docRepository.saveLatestRedisEntry(docId, "redis-entry-id") }
        }
    }


    @Nested
    @DisplayName("When parentUuid is null")
    inner class WithoutParent {
        @Test
        @DisplayName("execute should move an element to root, publish the event, and update the repo")
        fun `execute should correctly move an element to root`() = runTest {
            // Given
            val elementUuid = UUID.randomUUID()

            val moveElement = MoveElement(uuid = elementUuid, parentUuid = null)
            val moveElementPayload = MoveElementPayload(element = moveElement)
            val editDocEventValue = EditDocEventValue(
                docId = docId,
                processId = processId,
                userId = 123L,
                windowId = 789L,
                payload = moveElementPayload
            )

            val elementToMove = ElementRAM(
                uuid = elementUuid,
                value = JsonNull,
                metadata = JsonNull,
                type = "shape",
                parentUuid = UUID.randomUUID(), // Assume it had a parent before
                element = null,
                children = linkedMapOf(),
                path = "",
                deletedAt = null
            )

            every { documentRam.searchElement(elementUuid) } returns elementToMove
            coEvery { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) } returns "redis-entry-id"
            coEvery { elementRepo.updateMovedElement(any(), any()) } returns 1

            val command = MoveElementCommand(
                editDocEventValue, docId, processId,
                documentRedisPublisher, elementRepo, documentStorage, logPublisher,
                documentRepository = docRepository
            )

            // When
            command.execute()


            // Then
            coVerify { documentRam.remove(elementToMove) }
            coVerify { documentRam.addRoot(any()) }
            coVerify { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
            coVerify {
                elementRepo.updateMovedElement(
                    uuid = elementUuid,
                    parentUuid = null
                )
            }

            coVerify { docRepository.saveLatestRedisEntry(docId, "redis-entry-id") }
        }
    }
}