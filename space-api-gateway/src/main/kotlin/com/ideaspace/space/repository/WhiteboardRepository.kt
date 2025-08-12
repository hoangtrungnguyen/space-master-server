package com.space.com.ideaspace.space.repository

import com.space.com.ideaspace.space.client.IdeaSpaceServerClient
import com.space.com.ideaspace.space.client.dto.SpaceDashboardItem
import com.space.com.ideaspace.space.models.BoardState
import java.util.concurrent.ConcurrentHashMap

class WhiteboardRepository(
//    val redisConsumer: RedisConsumerService,
    val syncServerClient: IdeaSpaceServerClient
) {
//    var mockBoardStates

    /**
     * A thread-safe, in-memory map to store mock board states.
     * Using ConcurrentHashMap is a good practice for server environments
     * where multiple requests might access this data simultaneously.
     */
    private val mockBoardStates = ConcurrentHashMap<String, BoardState>().apply {
        put("board-1", BoardState(id = "board-1", title = "Hello, World!", revision = 1))
        put("board-2", BoardState(id = "board-2", title = "Initial content for board 2.", revision = 0))
        put("board-3", BoardState(id = "board-3", title = "", revision = 0))
    }


    fun boardExists(boardId: String): Boolean {
        return mockBoardStates.keys.contains(boardId)
    }

    suspend fun getBoardState(boardId: String): BoardState? {
        return mockBoardStates[boardId]
    }


    suspend fun createBoard(): String {



        return ""
    }

    suspend fun findAll(): List<SpaceDashboardItem> {
        val response = syncServerClient.getAllSpace()
        return response
    }
}