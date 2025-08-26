package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.RemoveElement
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
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
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RemoveElementCommandTest {

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


        every { documentStorage.documentsMap[docId] } returns documentRam
    }

    @Test
    fun `execute should log a warning if element not found`() = runTest {
        // Given
        val elementUuid = UUID.randomUUID()
        val removeElement = RemoveElement(uuid = elementUuid)
        val removeElementPayload = RemoveElementPayload(element = removeElement)
        val editDocEventValue = EditDocEventValue(
            docId = docId,
            processId = processId,
            userId = 123L,
            windowId = 789L,
            payload = removeElementPayload
        )

        every { documentRam.searchElement(elementUuid) } returns null
        coEvery { logPublisher.warn(any(), toLogServer = any()) } just runs

        val command = RemoveElementCommand(
            editDocEventValue, docId, processId,
            documentRedisPublisher, elementRepo, documentStorage, logPublisher,
            docRepository
        )

        // When
        command.execute()

        // Then
        coVerify(exactly = 1) { logPublisher.warn(any(), toLogServer = any()) }
        coVerify(exactly = 0) { documentRam.remove(any()) }
        coVerify(exactly = 0) { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) }
        coVerify(exactly = 0) { elementRepo.deleteByUuid(any()) }
        coVerify(exactly = 0) { docRepository.saveLatestRedisEntry(any(), any()) }
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
            windowId = 789L,
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
        coEvery {
            documentRedisPublisher.publishEditDocEvent(
                any(), any(), any()
            )
        } returns "redis-entry-id"
        coEvery { elementRepo.deleteByUuid(elementUuid) } returns true

        val command = RemoveElementCommand(
            editDocEventValue, docId, processId,
            documentRedisPublisher, elementRepo, documentStorage, logPublisher,
            docRepository
        )

        // When
        command.execute()

        // Then
        coVerify { documentRam.remove(elementToRemove) }
        coVerify { documentRedisPublisher.publishEditDocEvent(docId, processId, any()) }
        coVerify { elementRepo.deleteByUuid(elementUuid) }
        coVerify { docRepository.saveLatestRedisEntry(docId, "redis-entry-id") }
    }
}