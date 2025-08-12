package org.example.com.ideaspace.core.models

import java.time.ZonedDateTime
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class BusinessDocument @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class) constructor(
    val id: Long,
    val uuid: Uuid,
    val dbaseRevId: Int,
    val title: String,
    val creatorId: Long,
    val ownerId: Long,
    val createdAt: ZonedDateTime,
    val lastModifiedAt: ZonedDateTime,
    val metadata: Map<String, Any>?, // Storing JSONB as a String. Consider using a JSON library like Gson or kotlinx.serialization for parsing.
    val documentType: DocumentType,
    val status: Status,
    val transformVersion: Long
) {

}


enum class DocumentType {
    CANVAS,
    WORD
}

/**
 * Enum representing the possible statuses for a document.
 * This corresponds to the 'status' column.
 */
enum class Status {
    ARCHIVE,
    DRAFT


}
