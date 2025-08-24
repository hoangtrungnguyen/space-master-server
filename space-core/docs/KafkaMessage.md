Brief: data class designs for each modules

# KafkaMessage

## Document's events

LOCATION: `com/ideaspace/core/kafkaMessage/DocumentSyncEvent.kt`

Type of document sync is defined in

```kotlin
enum class SyncOperation {
    INIT_SYNC,
    EDIT_DOC,
    SAVE_DOC,
    FINISH_SYNC
}
```

- INIT_SYNC:
    - will be sent from a window whenever a document's page is opened
- FINISH_SYNC: will be produced to Kafka Stream when client processed all data from REDIS Stream
    - This event notifies server this client has already consumed all data.
    - 