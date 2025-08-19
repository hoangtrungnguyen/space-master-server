package com.ideaspace.document

import com.ideaspace.core.repository.CrudDocumentRepository

class SaveLatestRedisEntry(
    val docId: Long,
    val redisEntry: String,
){
    suspend fun execute(
        documentRepository: CrudDocumentRepository
    ){
        documentRepository.saveLatestRedisEntry(docId, redisEntry)
    }
}