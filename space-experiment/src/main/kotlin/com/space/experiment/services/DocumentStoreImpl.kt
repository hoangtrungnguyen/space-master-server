package com.space.experiment.services

import com.space.experiment.domain.OperationLogEntry

class DocumentHandlerImpl(
) : OperationLog {

    val logs = mutableListOf<OperationLogEntry>()
    override fun getOpsSinceRevision(revision: Long): List<OperationLogEntry> {
        return logs.filter { it.revision <= revision }
    }

    override fun saveLogEntry(logEntry: OperationLogEntry) {
        logs.add(logEntry)
    }

}