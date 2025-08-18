package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.AddElement
import com.ideaspace.core.kafkaMessage.AddElementPayload
import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.ElementOp
import com.ideaspace.core.kafkaMessage.SyncOperation
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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class AddElementCommandTest {
    private lateinit var documentRedisPublisher: DocumentRedisPublisher
    private lateinit var elementRepo: ElementRepo
    private lateinit var documentStorage: DocumentStorage
    private lateinit var documentRam: DocumentRAM

    private val docId = 1L
    private val processId = 100L
    private val userId = 1234L
    private val sessionId = 7777L
    private val clientId = 123L
    @BeforeEach
    fun setUp() {
        documentRedisPublisher = mockk()
        elementRepo = mockk()
        documentStorage = mockk()
        documentRam = mockk(relaxed = true) // relaxed to avoid mocking every single method

        // Common setup for all tests
        coEvery { documentRedisPublisher.publishEditDocEvent(any(), any(), any()) } just runs
        coEvery { elementRepo.insert(any()) } returns mockk()
        every { documentStorage.documentsMap } returns mutableMapOf(docId to documentRam)
    }


    @Nested
    @DisplayName("parent uuid is empty")
    inner class ParentIsEmpty{

        @Test
        fun `execute should add a root element when parentUuid is null`() = runTest {

        }
    }



    @Test
    fun getEditDocEventValue() {
    }

    @Test
    fun getDocId() {
    }

    @Test
    fun getProcessId() {
    }

}