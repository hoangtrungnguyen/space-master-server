package com.ideaspace.document

import com.ideaspace.core.models.BusinessDocument
import com.ideaspace.core.ram.DocumentRAM
import com.ideaspace.core.ram.toRAM
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InitSyncDocument(
    val doc: BusinessDocument,
    val processId: Long,
    ) {

    suspend fun execute(
        documentPublisher: DocumentRedisPublisher,
        documentStorage: DocumentStorage,
        elementRepo: ElementRepo
    ) {

        val elements = elementRepo.findAllByDocId(doc.id)

        val rootElements =
            ConcurrentHashMap(elements.filter { it.parentUuid == null }.map {
                it.toRAM()
            }.associateBy { it.uuid })

        val documentRAM = DocumentRAM(
            id = doc.id,
            title = doc.title,
            roots = rootElements
        )

        elements.filter { it.parentUuid != null }.forEach {
            documentRAM.addElement(
                rootElements[it.parentUuid]!!, it.parentUuid!!,
                it.toRAM()
            )
        }

        documentPublisher.publishDocProcess(doc.id, processId)

        documentStorage.add(
            doc.id,
            documentRAM
        )

        print("✅ InitSyncDocument okay")
    }
}