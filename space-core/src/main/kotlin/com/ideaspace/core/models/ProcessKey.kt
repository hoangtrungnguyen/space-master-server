package com.ideaspace.core.models

/**
 * Value class representing a composite key for Process entities.
 * Combines docId, userId, and windowId into a single string representation.
 */
@JvmInline
value class ProcessKey(val value: String) {

    constructor(docId: Long, userId: Long, windowId: Long) : this("$docId:$userId:$windowId")

    /**
     * Extracts the docId from the composite key
     */
    val docId: Long
        get() = value.split(":")[0].toLong()

    /**
     * Extracts the userId from the composite key
     */
    val userId: Long
        get() = value.split(":")[1].toLong()

    /**
     * Extracts the windowId from the composite key
     */
    val windowId: Long
        get() = value.split(":")[2].toLong()

    /**
     * Validates that the key format is correct
     */
    fun isValid(): Boolean {
        return try {
            val parts = value.split(":")
            parts.size == 3 && parts.all { it.toLongOrNull() != null }
        } catch (e: Exception) {
            false
        }
    }

    override fun toString(): String = value

    companion object {
        /**
         * Creates a ProcessKey from a string, with validation
         */
        fun fromString(value: String): ProcessKey? {
            val key = ProcessKey(value)
            return if (key.isValid()) key else null
        }

        /**
         * Creates a ProcessKey from individual components
         */
        fun of(docId: Long, userId: Long, windowId: Long): ProcessKey {
            return ProcessKey(docId, userId, windowId)
        }
    }
}