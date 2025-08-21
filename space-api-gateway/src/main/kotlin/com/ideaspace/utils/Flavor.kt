package com.ideaspace.utils

import io.ktor.server.application.*

val ApplicationEnvironment.isDevMode get() = this.config.property("flavor").getString() == "dev"
val ApplicationEnvironment.isLocalMode get() = this.config.property("flavor").getString() == "local"
