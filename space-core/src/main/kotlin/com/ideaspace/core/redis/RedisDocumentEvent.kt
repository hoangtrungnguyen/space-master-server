

fun redisDocKey(docId: Long): String {
    return "document:$docId"
}

fun redisDocSyncEventsKey(docId: Long): String {
    return "ideaspace:doc:$docId:stream"
}

fun redisPeer2PeerEventsKey(docId: Long): String {
    return "ideaspace:doc:$docId:process:active"
}