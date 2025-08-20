package com.ideaspace.workers

import com.ideaspace.core.ram.DocumentRAM
import java.util.UUID


interface DocumentStorageInterface {
    fun add(documentId: Long , documentRAM: DocumentRAM)
    fun findByUuid()
    fun exist(id: Long) : Boolean
}

class DocumentStorage(
    val documentsMap: MutableMap<Long, DocumentRAM>
) : DocumentStorageInterface {

    override fun add(documentId: Long , documentRAM: DocumentRAM){
        documentsMap[documentId] = documentRAM
    }

    override fun findByUuid(){

    }

    override fun exist(id: Long) : Boolean {
        return documentsMap.containsKey(id)
    }


}