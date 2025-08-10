package com.space.features.space.models

import kotlinx.serialization.Serializable

// --- Data classes representing the state of a whiteboard ---
// These would likely live in a 'model' sub-package.

@Serializable
data class Shape(val id: String, val type: String, val x: Double, val y: Double, val color: String)

@Serializable
data class TextBox(val id: String, val text: String, val x: Double, val y: Double, val fontSize: Int)

@Serializable
data class Document(
    val id: String,
    val text: String
)

/**
 * Represents the complete initial state of a whiteboard.
 * This object is serialized and sent to a user immediately upon joining a session.
 */
@Serializable
data class BoardState(
    val id: String,
    val title: String,
    val shapes: List<Shape> = emptyList(),
    val textboxes: List<TextBox> = emptyList(),
    val documents: List<Document> = emptyList(),
    val revision: Int
    // You could add other elements here, like images, tables, etc.
)
