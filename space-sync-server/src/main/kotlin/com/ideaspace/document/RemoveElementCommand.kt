package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.EditDocPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.models.Element
import com.ideaspace.core.redis.toRedisDocumentEvent
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
import kotlin.time.Clock

class RemoveElementCommand(
    val editDocEventValue: EditDocEventValue,
    val docId: Long,
    val processId: Long,
    val documentRedisPublisher: DocumentRedisPublisher,
    val elementRepo: ElementRepo,
    val documentStorage: DocumentStorage,
    val logPublisher: LogPublisher,
    override val documentRepository: CrudDocumentRepository
) : BaseDocCommand{

    override suspend fun execute() {
        val removeElementPayload = editDocEventValue.payload as RemoveElementPayload
        val element = removeElementPayload.element
        val document = documentStorage.documentsMap[docId]!!
        val prevElementRAM = document.searchElement(element.uuid)

        if (prevElementRAM != null) {
            document.remove(prevElementRAM)

            val redisEntryId = documentRedisPublisher.publishEditDocEvent(
                docId,
                processId,
                editDocEventValue.toRedisDocumentEvent()
            )

            documentRepository.saveLatestRedisEntry(docId, redisEntryId)
            println("✅ Remove element ${element.uuid}")
            withContext(currentCoroutineContext() + Dispatchers.IO) {
                elementRepo.deleteByUuid(
                    element.uuid
                )
            }
        } else {
            //duplicated element.uuid
            logPublisher.warn(
                toLogServer = true,
                event = LogData(
                    loggerName = "RemoveElementCommand",
                    message = Json.encodeToJsonElement(editDocEventValue),
                    userId = editDocEventValue.userId,
                    docId = docId,
                    processId = processId,
                    exceptionInfo = "⚠️ Element uuid ${element.uuid} not found"
                )
            )
        }
    }
}