package com.chris.chipherlink.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Manages AI providers and handles provider selection and fallback.
 *
 * Architecture:
 * - Supports multiple providers (cloud, local, user-selected)
 * - Falls back to next provider if current is unavailable
 * - Respects privacy permissions before sending data
 */
class AiProviderManager {

    private val providers = mutableListOf<AiProvider>()
    private var _selectedProviderId: String? = null

    val selectedProviderId: String? get() = _selectedProviderId

    /**
     * Register a new AI provider.
     */
    fun registerProvider(provider: AiProvider) {
        providers.add(provider)
        if (_selectedProviderId == null) {
            _selectedProviderId = provider.id
        }
    }

    /**
     * Select a specific provider.
     */
    fun selectProvider(providerId: String) {
        if (providers.any { it.id == providerId }) {
            _selectedProviderId = providerId
        }
    }

    /**
     * Get all available providers.
     */
    fun getAvailableProviders(): List<AiProvider> = providers.toList()

    /**
     * Send a message using the selected provider.
     * Falls back to other providers if the selected one is unavailable.
     */
    fun sendMessage(
        messages: List<AiMessage>,
        systemPrompt: String? = null
    ): Flow<String> = flow {
        val selectedProvider = providers.find { it.id == _selectedProviderId }

        if (selectedProvider != null && selectedProvider.isAvailable()) {
            selectedProvider.sendMessage(messages, systemPrompt).collect { emit(it) }
            return@flow
        }

        // Fallback: try each provider
        for (provider in providers) {
            if (provider.isAvailable()) {
                provider.sendMessage(messages, systemPrompt).collect { emit(it) }
                return@flow
            }
        }

        throw IllegalStateException("No AI provider available")
    }

    /**
     * Check if any provider is available.
     */
    suspend fun hasAvailableProvider(): Boolean {
        return providers.any { it.isAvailable() }
    }
}
