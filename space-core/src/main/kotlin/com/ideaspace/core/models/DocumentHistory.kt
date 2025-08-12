package org.example.com.ideaspace.core.models

import java.time.ZonedDateTime


data class DocumentJournal(
    val id: Long,
    val docId: Long,
    val revId: Long,
    val payload: String // Storing JSONB as a String. Consider using kotlinx.serialization for structured access.
)

data class DocumentRevision(
    val id: Long,
    val docId: Long,
    val snapshot: Int,
    val savedAt: ZonedDateTime,
    val savedBy: Long,
    val status: RevisionStatus
)

enum class RevisionStatus {
    ACTIVE,
    SUPERSEDED,
    ARCHIVED
}


data class DocumentSnapshot(
    val id: Long,
    val docId: Long,
    val revisionId: Long,
    val data: String, // JSONB payload containing the document and all its elements (nodes).
    val createdAt: ZonedDateTime
)
