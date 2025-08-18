package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.AddElementPayload
import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.models.Element
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.redis.toRedisEditPayLoad
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import io.ktor.server.plugins.di.DependencyRegistry
import java.util.LinkedHashMap
import kotlin.time.ExperimentalTime

class AddElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long
) {

    @OptIn(ExperimentalTime::class)
    suspend fun execute(
        documentRedisPublisher: DocumentRedisPublisher,
        elementRepo: ElementRepo,
        documentStorage: DocumentStorage
    ) {

        val addElementPayload =editDocEventValue.payload as AddElementPayload

        val element = addElementPayload.element
        val elementOp = addElementPayload.elementOp

        val document = documentStorage.documentsMap[docId]!!
        if (element.parentUuid == null) {
            document.addRoot(
                element.uuid, ElementRAM(
                    uuid = element.uuid,
                    element = null,
                    value = element.value,
                    metadata = element.metadata,
                    path = element.uuid.toString(),
                    children = LinkedHashMap(),
                    type = element.type,
                    parentUuid =null ,
                    deletedAt = null
                )
            )
        } else {
            assert(documentStorage.documentsMap[docId]!!.roots[element.parentUuid] != null)
            document.addElement(
                element.parentUuid!!, ElementRAM(
                    uuid = element.uuid,
                    element = null,
                    value = element.value,
                    metadata = element.metadata,
                    path = "",
                    type = element.type,
                    parentUuid = element.parentUuid,
                    children = LinkedHashMap(),
                    deletedAt = null
                )
            )
        }

        val payload = editDocEventValue.toRedisDocumentEvent()
        documentRedisPublisher.publishEditDocEvent(docId,processId, payload )
        elementRepo.insert(
            Element(
                uuid = element.uuid,
                docId = docId,
                parentUuid = element.parentUuid,
                metadata = element.metadata,
                type = element.type,
                value = element.value,
                deletedAt = null
            )
        )
    }
}