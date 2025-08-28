package com.ideaspace.core.redis

import io.lettuce.core.codec.RedisCodec
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class StringByteArrayCodec : RedisCodec<String, ByteArray> {

    override fun decodeKey(bytes: ByteBuffer): String {
        return StandardCharsets.UTF_8.decode(bytes).toString()
    }

    override fun decodeValue(bytes: ByteBuffer): ByteArray {
        val arr = ByteArray(bytes.remaining())
        bytes.get(arr)
        return arr
    }

    override fun encodeKey(key: String): ByteBuffer {
        return StandardCharsets.UTF_8.encode(key)
    }

    override fun encodeValue(value: ByteArray): ByteBuffer {
        return ByteBuffer.wrap(value)
    }
}

fun redisDocSyncEventsKey(docId: Long): String {
    return "ideaspace:doc:$docId:stream"
}