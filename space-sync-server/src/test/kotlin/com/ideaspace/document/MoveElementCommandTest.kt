package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.MoveElement
import com.ideaspace.core.kafkaMessage.MoveElementPayload
import com.ideaspace.core.ram.DocumentRAM
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
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

    private val docId = 1L
    private val processId = 100L

    @BeforeEach
    fun setUp() {
        documentRedisPublisher = mockk(relaxed = true)
        elementRepo = mockk(relaxed = true)
        documentStorage = mockk(relaxed = true)
        documentRam = mockk(relaxed = true)

        every { documentStorage.documentsMap[docId] } returns documentRam
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
                sessionId = 456L,
                clientId = 789L,
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

            val command = MoveElementCommand(editDocEventValue, docId, processId)

            // When
            command.execute(documentRedisPublisher, elementRepo, documentStorage)

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
                sessionId = 456L,
                clientId = 789L,
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

            val command = MoveElementCommand(editDocEventValue, docId, processId)

            // When
            command.execute(documentRedisPublisher, elementRepo, documentStorage)

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
        }
    }
}