package org.example.com.ideaspace.core.models

import java.time.ZonedDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class Element @OptIn(ExperimentalUuidApi::class) constructor(
    val uuid: Uuid,
    val docId: Long,
    val parentUuid: Uuid?,
    val metadata: Map<String, Any>?, // Storing JSONB as a String. Use kotlinx.serialization for parsing.
    val type: ElementType,
    val value: String, // Storing JSONB as a String. The structure will vary based on the 'type'.
    val deletedAt: ZonedDateTime?
){
    init {
        // This check ensures the 'value' string is not empty or just whitespace.
        require(value.isNotBlank()) { "Element 'value' cannot be blank." }

        // You can add more complex validation here to ensure the 'value'
        // structure is consistent with the 'type'.
        // For example (this is just a conceptual illustration):
        when (type) {
            ElementType.LINK -> require(value.startsWith("http")) {
                "Link element value must be a valid URL."
            }
            ElementType.SHAPE -> {
                // Here you might parse the JSON in 'value' and check for
                // required fields like 'x', 'y', 'width', 'height'.
            }
            // ... add checks for other types
            else -> {
                // No specific validation for other types
            }
        }
    }

}


enum class ElementType {
    TABLE,
    LINK,
    SHAPE,
    SECTION,
    FRAME
}
