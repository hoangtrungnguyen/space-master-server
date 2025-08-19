package com.ideaspace.core.repository

import com.ideaspace.core.models.Process

interface ProcessRepo {
    suspend fun create(process: Process): Process
    
    suspend fun findById(id: Long): Process?
    
    suspend fun findByDocUserWindow(docId: Long, userId: Long, windowId: Long): Process?
    
    suspend fun findAll(): List<Process>
    
    suspend fun update(process: Process): Process?
    
    suspend fun deleteById(id: Long): Boolean
    
    suspend fun existsById(id: Long): Boolean
    
    suspend fun existsByDocUserWindow(docId: Long, userId: Long, windowId: Long): Boolean
}