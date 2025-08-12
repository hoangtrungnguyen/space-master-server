package org.example.com.ideaspace.core.models

data class User(val userId: String, val subscription: Subscription)

data class Subscription(val name: String)
