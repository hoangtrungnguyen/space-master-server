package com.space.com.ideaspace.space.client

import com.space.com.ideaspace.space.client.dto.SpaceDashboardItem
import com.space.com.ideaspace.space.client.dto.SpaceDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class IdeaSpaceServerClient(private val httpClient: HttpClient) {

    suspend fun initBoardState(boardId: String) {
        httpClient.get("/init/{boardId}") { }
    }

    suspend fun createSpace(): Map<String, String> {
        val response = httpClient.post("/api/space/create")
        return response.body<Map<String, String>>()
    }

    suspend fun getAllSpace(): List<SpaceDashboardItem> {
        val response = httpClient.get("/api/space/all")
        return response.body<List<SpaceDashboardItem>>()
    }

    suspend fun getById(id: String): SpaceDto? {
        try {
            val response = httpClient.get("/api/space/${id}")
            return response.body<SpaceDto>()
        } catch (e: Exception){
            return null
        }
    }
}