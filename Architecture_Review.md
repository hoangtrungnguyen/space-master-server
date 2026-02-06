# Architecture Review: Space Master Server

**Date**: 2026-02-06
**Project**: Space Master Server (Collaborative Whiteboard)

## 1. Executive Summary

The `space-master-server` is a collaborative whiteboard application designed with a microservices architecture. It separates connection management and authentication (`space-api-gateway`) from core business logic and data consistency (`space-sync-server`). The system leverages **Kafka** as an event bus for reliable upstream communication and **Redis** (Streams + Keyspace Notifications) for low-latency downstream broadcasting.

## 2. System Architecture

The system follows an **Event-Driven Architecture** combined with **Command Query Responsibility Segregation (CQRS)** principles effectively separating the write path (upstream) from the read/broadcast path (downstream).

### High-Level Components

*   **Space API Gateway**: 
    *   **Role**: Entry point for Frontend clients. Handles WebSockets, Authentication (JWT), and simple REST APIs (User management).
    *   **Responsibilities**: Authentication, converting WebSocket messages to Kafka events, listening to Redis for updates to broadcast back to clients.
*   **Space Sync Server**:
    *   **Role**: Backend processor.
    *   **Responsibilities**: Consumes events from Kafka, executes business logic (e.g., conflicting edits resolution), updates the permanent database (Postgres), and pushes state changes to Redis.
*   **Space Core**:
    *   **Role**: Shared library.
    *   **Responsibilities**: Shared data models, DTOs, Database repositories (Exposed), and utility configurations (Kafka, Redis).

### Architecture Diagram

```mermaid
graph TD
    Client[Client (FE)]
    
    subgraph "Infrastructure"
        K[Kafka]
        R[Redis]
        DB[(Postgres)]
    end

    subgraph "Services"
        GW[API Gateway]
        SS[Sync Server]
    end

    %% Upstream Flow
    Client -- "1. VS / WebSocket Info" --> GW
    GW -- "2. Produce Event" --> K
    K -- "3. Consume Event" --> SS
    
    %% Processing
    SS -- "4. Persistence" --> DB
    
    %% Downstream Flow
    SS -- "5. XADD (Stream)" --> R
    R -. "6. Keyspace Notification" .-> GW
    GW -- "7. Fetch Stream Entry" --> R
    GW -- "8. Broadcast Update" --> Client
```

## 3. Data Flow Analysis

### 3.1 Upstream (Code to Cloud)
1.  **Ingestion**: Frontend establishes a WebSocket connection to `space-api-gateway`.
2.  **Authentication**: Connection is authenticated via JWT.
3.  **Event Production**: User actions (e.g., drawing, moving elements) are received as JSON. The Gateway wraps these into `DocumentSyncEventValue` and publishes them to the Kafka topic (default: `document_events`).
4.  **Ordering**: Accessing a single partition per document (implied by using standard Kafka hashing on Document ID keys) ensures strict ordering of events for a given whiteboard.

### 3.2 Downstream (Cloud to Code)
1.  **Consumption**: `space-sync-server` consumes the Kafka event.
2.  **Processing**: The `KafkaPartitionProcessor` handles the business logic.
3.  **Broadcasting**: Validated changes are written to a Redis Stream via `XADD`.
4.  **Notification**: The Gateway subscribes to Redis Keyspace Notifications (`__keyspace@0__:ideaspace:doc:<id>:sync-events`).
5.  **Delivery**: Upon receiving a notification (specifically for `xadd` operation), the Gateway queries the latest entry from the Redis Stream and broadcasts it to all connected WebSocket clients for that document.

## 4. Key Design Patterns

*   **Microservices**: Clear separation of `Gateway` (I/O bound) and `Sync` (CPU/Logic bound).
*   **Event Sourcing**: Kafka acts as the log of all state changes, allowing for potential replayability and recovery.
*   **Shared Kernel**: `space-core` ensures that DTOs and Logic are consistent between services.
*   **Publish-Subscribe (Hybrid)**: Uses Kafka for reliable inter-service pub/sub and Redis Strings+Notifications for real-time frontend pub/sub.

## 5. Technology Stack Assessment

| Component | Technology | Reasoning / Review |
| :--- | :--- | :--- |
| **Language** | Kotlin (JVM) | Modern, type-safe, excellent coroutine support for high concurrency. |
| **Framework** | Ktor | Lightweight, async-first, fits well for WebSockets and microservices. |
| **Database** | Postgres (Exposed) | Reliable relational storage. `Exposed` provides a type-safe DSL. |
| **Message Broker** | Kafka | High throughput, durability, strict ordering per partition. Excellent for this use case. |
| **Cache/Real-time**| Redis (Lettuce) | Used for both caching and real-time signaling (Keyspace notifications). |
| **Dependency Inj.**| Koin | Lightweight DI, idiomatic for Kotlin/Ktor. |

## 6. Recommendations & Observations

### Strengths
1.  **Scalability**: Gateways can be scaled horizontally easily as they are stateless (except for active WS connections). Sync Servers can be scaled based on Kafka partitions.
2.  **Resilience**: Kafka provides a buffer. If Sync Server goes down, events are persisted and processed when it returns.
3.  **Low Latency**: Redis Keyspace notifications provide a very fast mechanism to signal updates compared to polling.

### Areas for Improvement / TODOs
1.  **Security**: `validateDocumentAccess` in `Sockets.kt` is currently a TODO. This is a critical security gap.
2.  **Redis Notification Reliability**: Redis PubSub (Keyspace notifications) is "fire and forget". If a Gateway blinks, it might miss a notification.
    *   *Mitigation*: The `PullStreamInput` logic likely handles initial sync/recovery, but transient drops might need client-side detection (e.g., missed sequence numbers).
3.  **Sync Server state**: `KafkaPartitionProcessor` implies stateful processing. Ensure graceful rebalancing if multiple Sync instances are running.
4.  **Complexity**: The "Notification -> Fetch" pattern in Redis Subscriber (`xadd` -> `xrevrange`) introduces a small race condition or overhead.
    *   *Alt*: Direct usage of Redis PubSub for the payload might be simpler if persistence isn't needed in Redis (since Kafka has it), but using Streams allows "catching up" for slightly disconnected clients.

## 7. Configuration Review
-   **Ports**: Gateway (8080), Sync (9099), Customer Insights (8095 - noted in ReadMe but not explored).
-   **Dependencies**: Project layout is standard Gradle multi-module. Good separation.
