package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.RemoveElement
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
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
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RemoveElementCommandTest {

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

    @Test
    @DisplayName("execute should remove an element, publish the event, and delete from the repo")
    fun `execute should remove an element`() = runTest {
        // Given
        val elementUuid = UUID.randomUUID()
        val removeElement = RemoveElement(uuid = elementUuid)
        val removeElementPayload = RemoveElementPayload(element = removeElement)
        val editDocEventValue = EditDocEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            sessionId = 456L,
            clientId = 789L,
            payload = removeElementPayload
        )

        val elementToRemove = ElementRAM(
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

        every { documentRam.searchElement(elementUuid) } returns elementToRemove
        coEvery { documentRedisPublisher.publishEditDocEvent(
            any(), any(), any()) } returns "redis-entry-id"
        coEvery { elementRepo.deleteByUuid(elementUuid) } returns true

        val command = RemoveElementCommand(editDocEventValue, docId, processId)

        // When
        command.execute(documentRedisPublisher, elementRepo, documentStorage)

        // Then
        coVerify { documentRam.remove(elementToRemove) }
        coVerify { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
        coVerify { elementRepo.deleteByUuid(elementUuid) }
    }
}