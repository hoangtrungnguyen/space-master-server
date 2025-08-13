package com.ideaspace.core.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: Long,
    var loginName: String,
    var fullName: String,
)
