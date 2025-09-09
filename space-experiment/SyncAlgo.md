# **Collaborative Sync Server Algorithm**

This project contains a Kotlin implementation of the server-side logic for a real-time collaborative application, based
on the provided flowchart. It uses the Operational Transformation (OT) pattern to handle concurrent edits from multiple
users.

### **Core Concepts**

1. **Operation**: A client's proposed change, containing a baseRevision and a set of patches.
2. **Revisioning**: Every document has a currentRevision number. When a client submits an operation, its baseRevision
   must match the server's currentRevision for a direct application.
3. **Conflict**: A conflict occurs when the client's baseRevision does not match the server's currentRevision. This
   means another user's change was accepted while the current client was preparing their own.
4. **Operational Transformation (OT)**: The conflict resolution strategy. When a conflict occurs, the incoming operation
   is "transformed" against the operations that have occurred on the server in the meantime. This adjusts the client's
   operation so it can be applied to the new, current state of the document.

### **Architectural Flow**

The code is structured around a central OperationProcessor that orchestrates the flow:

1. **Ingestion (OperationQueue)**: A client sends an operation via WebSocket. The server immediately places it into a
   persistent queue like Kafka or Redis. This ensures durability and decouples ingestion from processing.
2. **Processing (OperationProcessor)**: A background worker process continuously dequeues operations.
3. **Revision Check**:
    * **If client.baseRevision \== server.currentRevision (No Conflict)**: The operation can be applied directly.
    * **If client.baseRevision \!= server.currentRevision (Conflict)**: The processor fetches all operations from the
      OperationLog that have occurred since the client's baseRevision.
4. **Transformation (OperationalTransformer)**: The client's operation is transformed against the conflicting server
   operations.
    * **On Success**: A new, transformed operation is created.
    * **On Failure**: If the conflict is unresolvable (a very rare case in well-designed OT systems), the process is
      aborted, and a NACK is sent.
5. **Atomic Transaction (DocumentStore)**:
    * The (potentially transformed) operation is applied to the document state.
    * The server's currentRevision for the document is incremented.
    * The new document state and the operation itself are saved to the database in a single atomic transaction. This
      guarantees consistency.
6. **Notification (WebSocketNotifier)**:
    * The successfully applied operation and new revision are broadcast to all *other* clients working on the same
      document.
    * A special ACK (acknowledgment) message is sent back to the *original* client, confirming success and providing the
      new revision number.

### **How to Run (Conceptual)**

In a real environment, you would:

1. Set up a message queue (e.g., Kafka).
2. Set up a PostgreSQL database with tables for documents and operation\_logs.
3. Implement the service interfaces (OperationQueue, DocumentStore, etc.) with clients for these services.
4. Run the OperationProcessor in one or more background worker instances.
5. Your WebSocket server would be responsible for receiving operations and enqueuing them.