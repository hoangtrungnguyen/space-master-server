package com.ideaspace.core.models

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
        require(value.isNotBlank()) { "Element 'value' cannot be blank." }

        when (type) {
            ElementType.LINK -> require(value.startsWith("http")) {
                "Link element value must be a valid URL."
            }
            ElementType.SHAPE -> {
                // Here you might parse the JSON in 'value' and check for
                // required fields like 'x', 'y', 'width', 'height'.
            }
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
