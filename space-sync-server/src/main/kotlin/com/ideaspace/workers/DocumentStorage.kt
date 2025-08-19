package com.ideaspace.workers

import com.ideaspace.core.ram.DocumentRAM


interface DocumentStorageInterface {
    fun add(documentId: Long , documentRAM: DocumentRAM)
    fun findByUuid()
}

class DocumentStorage(
    val documentsMap: MutableMap<Long, DocumentRAM>
) : DocumentStorageInterface {

    override fun add(documentId: Long , documentRAM: DocumentRAM){
        documentsMap[documentId] = documentRAM
    }

    override fun findByUuid(){

    }

}