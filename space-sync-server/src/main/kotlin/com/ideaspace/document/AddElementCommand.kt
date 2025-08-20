package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.AddElementPayload
import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.models.Element
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.redis.toRedisEditPayLoad
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.utils.LogData
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import io.ktor.server.plugins.di.DependencyRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.LinkedHashMap
import kotlin.time.ExperimentalTime

class AddElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long,
    val documentRedisPublisher: DocumentRedisPublisher,
    val elementRepo: ElementRepo,
    val documentStorage: DocumentStorage,
    val logPublisher: LogPublisher,
    override val documentRepository: CrudDocumentRepository
) : BaseDocCommand{

    @OptIn(ExperimentalTime::class)
    override suspend fun execute() {

        val addElementPayload = editDocEventValue.payload as AddElementPayload

        val element = addElementPayload.element
        val elementOp = addElementPayload.elementOp

        val document = documentStorage.documentsMap[docId]!!
        if (document.exist(element.uuid)) {
            logPublisher.warn(
                toLogServer = true,
                event = LogData(
                    loggerName = this.javaClass.simpleName.toString(),
                    message = Json.encodeToJsonElement(editDocEventValue),
                    userId = editDocEventValue.userId,
                    docId = docId,
                    processId = processId,
                    exceptionInfo = "⚠️ Element uuid ${element.uuid} is existed"
                )
            )
            return
        }

        if (element.parentUuid == null) {
            document.addRoot(
                ElementRAM(
                    uuid = element.uuid,
                    element = null,
                    value = element.value,
                    metadata = element.metadata,
                    path = element.uuid.toString(),
                    children = LinkedHashMap(),
                    type = element.type,
                    parentUuid = null,
                    deletedAt = null
                )
            )
        } else {
            document.addElement(
                ElementRAM(
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
        val redisEntry = documentRedisPublisher.publishEditDocEvent(docId, processId, payload)
        super.saveLatestRedisEntry(docId, redisEntry)

        withContext(currentCoroutineContext() + Dispatchers.IO) {
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
}