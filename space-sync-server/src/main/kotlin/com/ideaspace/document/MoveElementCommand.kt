package com.ideaspace.document

import com.ideaspace.core.kafkaMessage.EditDocEventValue
import com.ideaspace.core.kafkaMessage.MoveElementPayload
import com.ideaspace.core.kafkaMessage.RemoveElementPayload
import com.ideaspace.core.redis.toRedisDocumentEvent
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.core.utils.LogData
import com.ideaspace.workers.DocumentStorage
import com.ideaspace.workers.LogPublisher
import io.lettuce.core.search.arguments.SugAddArgs.Builder.payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.ExperimentalTime

// from element to different group or no-group
@OptIn(ExperimentalTime::class)
class MoveElementCommand(
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
        val removeElementPayload = editDocEventValue.payload as MoveElementPayload
        val element = removeElementPayload.element
        val document = documentStorage.documentsMap[docId]!!

        val prevElementRAM = document.searchElement(element.uuid)

        if (prevElementRAM == null) {
            logPublisher.warn(
                toLogServer = true, event = LogData(
                    loggerName = this::class.simpleName.toString(),
                    message = Json.encodeToJsonElement(editDocEventValue),
                    userId = editDocEventValue.userId,
                    docId = docId,
                    processId = processId,
                    exceptionInfo = "⚠️ Element uuid ${element.uuid} not found"
                )
            )
            return
        }

        //remove
        document.remove(prevElementRAM)

        //add
        if (element.parentUuid == null) {
            document.addRoot(
                prevElementRAM.copy(
                    parentUuid = null,
                )
            )
        } else {
            document.addElement(
                prevElementRAM.copy(
                    parentUuid = element.parentUuid!!,
                )
            )
        }

        //publish changes
        val redisEntryId =
            documentRedisPublisher.publishEditDocEvent(docId, processId, editDocEventValue.toRedisDocumentEvent())
        documentRepository.saveLatestRedisEntry(docId, redisEntryId)

        println("✅ Moved element ${element.uuid}")
        withContext(currentCoroutineContext() + Dispatchers.IO) {
            elementRepo.updateMovedElement(
                uuid = element.uuid,
                parentUuid = element.parentUuid
            )
        }

    }
}