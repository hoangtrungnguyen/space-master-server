import com.ideaspace.core.kafkaMessage.DocumentSyncEventValue
import com.ideaspace.core.kafkaMessage.ElementOp
import com.ideaspace.core.kafkaMessage.SyncOperation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DocumentSyncEventTest {

    private val json = Json {
        ignoreUnknownKeys = true // Good practice for evolving APIs
    }

    @Test
    fun `should deserialize DocumentSyncEventValue from JSON string`() {
        // Given
        val jsonString = """
        {
            "sync_op": "EDIT_DOC",
            "doc_id": 98573498573,
            "process_id": 1672531200123,
            "user_id": 42,
            "session_id": 123456789,
            "client_id": 9876,
            "payload": {
                "element_op": "EDIT_ELEMENT",
                "element": {
                    "uuid": "c3a4e5f6-1234-5678-90ab-cdef12345678",
                    "parent_uuid": "d4e5f6a7-2345-6789-01bc-def012345678",
                    "metadata": {
                        "timestamp": 1672531200123,
                        "source": "web-client"
                    },
                    "type": "HEADING",
                    "value": {
                        "level": 1,
                        "text": "Welcome to the Document"
                    }
                }
            }
        }
        """.trimIndent()

        // When
        val event = json.decodeFromString<DocumentSyncEventValue>(jsonString)

        // Then
        assertEquals(SyncOperation.EDIT_DOC, event.syncOp)
        assertEquals(98573498573, event.docId)
        assertEquals(1672531200123L, event.processId)
        assertEquals(42L, event.userId)
        assertEquals(123456789L, event.sessionId)
        assertEquals(9876L, event.clientId)

        val payload = event.payload
        assertEquals<Enum<*>>(ElementOp.EDIT_ELEMENT, payload.elementOp)

        val element = payload.element
        assertEquals(UUID.fromString("c3a4e5f6-1234-5678-90ab-cdef12345678"), element.uuid)
        assertEquals(UUID.fromString("d4e5f6a7-2345-6789-01bc-def012345678"), element.parentUuid)
        assertEquals("HEADING", element.type)

        val expectedMetadata = buildJsonObject { put("timestamp", 1672531200123L); put("source", "web-client") }
        assertEquals(expectedMetadata, element.metadata)

        val expectedValue = buildJsonObject { put("level", 1); put("text", "Welcome to the Document") }
        assertEquals(expectedValue, element.value)
    }
}