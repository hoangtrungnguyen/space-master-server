package com.space.transform


import com.space.core.DocumentStore
import com.space.core.OperationHandler
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OperationHandlerTest {

    private lateinit var handler: OperationHandler

    // This function runs before each test to ensure a clean state
    @BeforeTest
    fun setup() {
        // Reset the singleton DocumentStore
        DocumentStore.document.clear()
        DocumentStore.document.addAll(listOf('H', 'e', 'l', 'l', 'o'))
        DocumentStore.revisionLog.clear()
        handler = OperationHandler()
    }

    @Test
    fun `test simple insert operation`() {
        val op = InsertOperation(char = '!', position = 5, clientVersion = 0, sourceId = "client-1")
        handler.handleOperation(op)
        assertEquals("Hello!", DocumentStore.getDocumentState())
    }

    @Test
    fun `test simple delete operation`() {
        val op = DeleteOperation(position = 4, clientVersion = 0, sourceId = "client-1")
        handler.handleOperation(op)
        assertEquals("Hell", DocumentStore.getDocumentState())
    }

    @Test
    fun `test concurrent inserts at same position`() {
        // C1 and C2 both start at version 0 ("Hello")
        val op1 = InsertOperation(char = '!', position = 5, clientVersion = 0, sourceId = "client-1")
        val op2 = InsertOperation(char = ' ', position = 5, clientVersion = 0, sourceId = "client-2")

        // Server receives op1 first
        handler.handleOperation(op1)
        assertEquals("Hello!", DocumentStore.getDocumentState())

        // Server receives op2, which is transformed against op1
        handler.handleOperation(op2)
        assertEquals(
            "Hello! ",
            DocumentStore.getDocumentState(),
            "The second insert should be transformed and applied correctly."
        )
    }

    @Test
    fun `test concurrent inserts at different positions`() {
        val op1 = InsertOperation(char = ' ', position = 0, clientVersion = 0, sourceId = "client-1") // " Hello"
        val op2 = InsertOperation(char = '!', position = 5, clientVersion = 0, sourceId = "client-2") // "Hello!"

        handler.handleOperation(op1)
        assertEquals(" Hello", DocumentStore.getDocumentState())

        handler.handleOperation(op2)
        assertEquals(" Hello!", DocumentStore.getDocumentState())
    }

    @Test
    fun `test concurrent deletes`() {
        // Initial state: "Hello"
        val op1 = DeleteOperation(position = 1, clientVersion = 0, sourceId = "client-1") // Deletes 'e'
        val op2 = DeleteOperation(position = 4, clientVersion = 0, sourceId = "client-2") // Deletes 'o'

        handler.handleOperation(op1)
        assertEquals("Hllo", DocumentStore.getDocumentState())

        // op2 is transformed: since a char was deleted before it, its position shifts from 4 to 3
        handler.handleOperation(op2)
        assertEquals("Hll", DocumentStore.getDocumentState())
    }

    @Test
    fun `test concurrent identical deletes`() {
        // Both clients try to delete 'e' at position 1
        val op1 = DeleteOperation(position = 1, clientVersion = 0, sourceId = "client-1")
        val op2 = DeleteOperation(position = 1, clientVersion = 0, sourceId = "client-2")

        handler.handleOperation(op1)
        assertEquals("Hllo", DocumentStore.getDocumentState())

        // The second op should be transformed into a no-op, so the state doesn't change
        handler.handleOperation(op2)
        assertEquals("Hllo", DocumentStore.getDocumentState())
    }

    @Test
    fun `test concurrent insert and delete`() {
        // C1 inserts 'a' at pos 0. C2 deletes 'o' at pos 4.
        val op1 = InsertOperation(char = 'a', position = 0, clientVersion = 0, sourceId = "client-1")
        val op2 = DeleteOperation(position = 4, clientVersion = 0, sourceId = "client-2")

        handler.handleOperation(op1)
        assertEquals("aHello", DocumentStore.getDocumentState())

        // op2 is transformed against op1. The insert at pos 0 shifts the delete to pos 5.
        handler.handleOperation(op2)
        assertEquals("aHell", DocumentStore.getDocumentState())
    }
}
