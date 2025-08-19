@file:OptIn(ExperimentalSerializationApi::class)

package com.ideaspace.session

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator


@Serializable
@JsonClassDiscriminator("type")
sealed class DocumentMessage

@Serializable
@SerialName("STREAM_ADD_ENTRY")
data class StreamAddEntry(
    @SerialName("seid")
    val streamEntryId: String
) : DocumentMessage()

