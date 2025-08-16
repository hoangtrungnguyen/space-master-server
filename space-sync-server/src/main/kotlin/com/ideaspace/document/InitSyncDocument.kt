package com.ideaspace.document

import com.ideaspace.core.dao.DocumentDAO
import com.ideaspace.core.ram.DocumentRAM
import com.ideaspace.core.ram.ElementRAM
import com.ideaspace.core.ram.toRAM
import com.ideaspace.core.repository.CrudDocumentRepository
import com.ideaspace.core.repository.ElementRepo
import com.ideaspace.workers.DocumentStorage
import kotlinx.serialization.json.JsonObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InitSyncDocument(
    val documentStorage: DocumentStorage,
    val elementRepo: ElementRepo
) {

    suspend fun execute(docDAO: DocumentDAO) {

        val elements = elementRepo.findAllByDocId(docDAO.id.value)

        val rootElements =
            ConcurrentHashMap(elements.filter { it.parentUuid == null }.map {
                it.toRAM()
            }.associateBy { it.uuid })

        val documentRAM = DocumentRAM(
            id = docDAO.id.value,
            title = docDAO.title,
            roots = rootElements
        )

        elements.filter { it.parentUuid != null }.forEach {
            documentRAM.addElement(
                rootElements[it.parentUuid]!!, it.parentUuid!!,
                it.toRAM()
            )
        }

        documentStorage.add(
            docDAO.id.value,
            documentRAM
        )

        print("InitSyncDocument okay")
    }
}