package com.space.features.space.service

import com.space.features.space.client.IdeaSpaceServerClient
import com.space.features.space.models.BoardState
import com.space.features.space.repository.WhiteboardRepository
import kotlinx.coroutines.delay

class IdeaSpaceSessionService(
    private val syncServerClient: IdeaSpaceServerClient,
    private val repository: WhiteboardRepository
) {
    // Constants for the polling mechanism
    companion object {
        private const val POLLING_INTERVAL_MS = 200L // Time to wait between checks
        private const val MAX_ATTEMPTS = 15          // Max number of checks before timeout (15 * 200ms = 3 seconds)
    }

    /**
     * Ensures a whiteboard session is valid and fetches its complete current state.
     *
     * This is the primary method called by the `WhiteboardSocketHandler` when a new user connects.
     * It first validates that the board exists before attempting to load its state.
     *
     * @param boardId The unique identifier for the whiteboard.
     * @return A [BoardState] object containing all the elements on the board.
     * @throws IllegalStateException if no whiteboard with the given ID is found.
     */
    suspend fun ensureActiveSessionAndGetState(boardId: String): BoardState {


        // First, check if the board actually exists in our persistent storage.
        if (!repository.boardExists(boardId)) {
            initBoardState(boardId)

            // --- Polling Logic ---
            var attempts = 0
            while (attempts < MAX_ATTEMPTS) {
                if (repository.boardExists(boardId)) {
                    // Session found, break the loop and proceed.
                    println("Session for board '$boardId' found after ${attempts + 1} attempts.")
                    break
                }
                attempts++
                delay(POLLING_INTERVAL_MS) // Wait before the next attempt
            }

            // After the loop, check if we timed out.
            if (attempts == MAX_ATTEMPTS) {
                throw IllegalStateException("Timed out waiting for session to be created for board ID: $boardId")
            }
        }

        println("Fetching state for active session: $boardId")

        return repository.getBoardState(boardId)
            ?: throw IllegalStateException("Board state for '$boardId' was null even after session was confirmed.")

    }

    private suspend fun initBoardState(boardId: String) {
        repository.getBoardState(boardId)
        //TODO call api to init board state in REDIS
        syncServerClient.initBoardState(boardId)
    }
}