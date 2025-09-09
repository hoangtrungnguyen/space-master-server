@file:OptIn(ExperimentalSerializationApi::class)

package com.space.experiment.domain

import com.ideaspace.core.kafkaMessage.EditElementEventValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


typealias EditTextOperation = EditElementEventValue

@JsonClassDiscriminator("type")
@Serializable
sealed class OperationPayload {
    abstract val revision: Long
}


/**
 * Value inside EditTExt [com.ideaspace.core.kafkaMessage.EditElementEventValue]
 */
@Serializable
@SerialName("INSERT_TEXT")
data class InsertText(
    val text: String,
    val position: Int,
    override val revision: Long
) : OperationPayload()

@Serializable
@SerialName("DELETE_TEXT")
data class DeleteText(
    val position: Int,
    val length: Int,
    override val revision: Long,
) : OperationPayload()


/**
 * Represents the complete state of a document at a specific revision.
 *
 * @property documentId The unique identifier of the document.
 * @property currentRevision The current version number of the document state.
 * @property content The actual content of the document (e.g., a JSON string or a complex object).
 */
@Serializable
data class DocumentState(
    val documentId: Long,
    var currentRevision: Long,
    var content: String // Using String for simplicity, could be a Map<String, Any> or a custom model
) {
    fun applyOp(operation: OperationPayload): DocumentState {
        when (operation) {
            is InsertText -> {
                val prefixSlice = content.slice(0 until operation.position)
                val postSlice = content.slice(operation.position until content.length)
                content = prefixSlice + operation.text + postSlice
            }

            is DeleteText -> {
                val preSlice = content.slice(0 until operation.position)

                val slice = content.slice(operation.position until operation.length)

                println("[DeleteText] slice: $slice")
                val postSlice = content.drop(operation.position + operation.length)

                content = preSlice + postSlice
            }
        }

        return this
    }
}


data class OperationLogEntry(
    val revision: Long,
    val operation: OperationPayload
)


//@Serializable
//data class AckTransformedOperation(
//    override val replyTo: String,
//    override val messageType: MessageType = MessageType.ACK_TRANSFORMED,
//    val message: String
//) : DocumentChannelOutput()

/**
 * Negative Acknowledgement sent to the original client on failure.
 *
 * @property success Always false.
 * @property reason A message explaining why the operation failed.
 */
data class Nack(
    val success: Boolean = false,
    val reason: String
)