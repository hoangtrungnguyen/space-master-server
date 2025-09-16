package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.AddElementEventValue
import com.ideaspace.core.models.Element
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.utils.LogData
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.ExperimentalTime

class AddElementCommand(
    val editDocEventValue: AddElementEventValue,
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
        val element = editDocEventValue

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

        val redisEntry = documentRedisPublisher.publishEditDocEvent(docId, processId, editDocEventValue)

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