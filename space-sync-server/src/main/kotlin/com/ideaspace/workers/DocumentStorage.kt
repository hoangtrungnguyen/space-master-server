package com.ideaspace.workers

import com.ideaspace.core.ram.DocumentRAM


interface DocumentStorageInterface {
    fun add(documentId: Long , documentRAM: DocumentRAM)
}

class DocumentStorage(
    val documentsMap: MutableMap<Long, DocumentRAM>
) : DocumentStorageInterface {

    override fun add(documentId: Long , documentRAM: DocumentRAM){
        documentsMap[documentId] = documentRAM
    }

}