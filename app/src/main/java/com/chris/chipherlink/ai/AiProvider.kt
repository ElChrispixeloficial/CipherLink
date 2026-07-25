package com.chris.chipherlink.ai

import kotlinx.coroutines.flow.Flow

/**
 * Interface for AI providers.
 * Supports both cloud and local implementations.
 *
 * Implementations:
 * - CloudAiProvider: Calls cloud API (OpenAI-compatible)
 * - LocalAiProvider: Future on-device model
 */
interface AiProvider {
    /** Unique identifier for this provider */
    val id: String

    /** Display name */
    val displayName: String

    /** Whether this provider is currently configured and available */
    suspend fun isAvailable(): Boolean

    /**
     * Send a message and receive a response.
     * Returns a Flow that emits chunks of the response as they arrive.
     *
     * @param messages Conversation history
     * @param systemPrompt Optional system prompt for context
     * @return Flow of response text chunks
     */
    fun sendMessage(
        messages: List<AiMessage>,
        systemPrompt: String? = null
    ): Flow<String>
}

data class AiMessage(
    val role: MessageRole,
    val content: String
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
