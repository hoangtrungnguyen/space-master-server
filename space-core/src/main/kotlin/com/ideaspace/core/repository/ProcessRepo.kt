package com.ideaspace.core.repository

import com.ideaspace.core.models.Process
import com.ideaspace.core.models.ProcessKey

interface ProcessRepo {
    suspend fun create(process: Process): Process
    
    suspend fun findById(id: Long): Process?
    
    suspend fun findByKey(key: ProcessKey): Process?
    
    suspend fun findAll(): List<Process>
    
    suspend fun update(process: Process): Process?
    
    suspend fun deleteById(id: Long): Boolean
    
    suspend fun existsById(id: Long): Boolean
    
    suspend fun existsByDocUserWindow(docId: Long, userId: Long, windowId: Long): Boolean
}