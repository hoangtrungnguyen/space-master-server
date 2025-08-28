package com.ideaspace.utils

import io.ktor.server.application.*
import java.util.UUID

val ApplicationEnvironment.isDevMode get() = this.config.property("flavor").getString() == "dev"
val ApplicationEnvironment.isLocalMode get() = this.config.property("flavor").getString() == "local"

fun String.toUUID() : UUID = UUID.fromString(this)