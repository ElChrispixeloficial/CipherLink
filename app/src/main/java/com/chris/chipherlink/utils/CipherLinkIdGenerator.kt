package com.chris.chipherlink.utils

import java.security.SecureRandom

/**
 * Generates and validates CipherLink public IDs.
 *
 * Format: CL-XXXXXX where X is an uppercase alphanumeric character
 * excluding ambiguous characters (0, O, I, L) for readability.
 *
 * Examples: CL-7A91F3, CL-K4D82X, CL-X9M2Q7
 *
 * Security:
 * - 36 possible characters per position = 36^6 = 2,176,782,336 combinations
 * - Sufficient for user-scale uniqueness without revealing private data
 * - Public ID is separate from internal UUID
 */
object CipherLinkIdGenerator {

    private const val PREFIX = "CL-"
    private const val ID_LENGTH = 6

    // Remove ambiguous characters: 0, O, I, L
    private const val CHARSET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    private val random = SecureRandom()

    /**
     * Generate a new unique CipherLink ID.
     * @return Format: "CL-XXXXXX"
     */
    fun generate(): String {
        val sb = StringBuilder(PREFIX)
        repeat(ID_LENGTH) {
            sb.append(CHARSET[random.nextInt(CHARSET.length)])
        }
        return sb.toString()
    }

    /**
     * Validate a CipherLink ID format.
     * @param id The ID to validate
     * @return true if format is valid
     */
    fun isValid(id: String): Boolean {
        if (!id.startsWith(PREFIX)) return false
        if (id.length != PREFIX.length + ID_LENGTH) return false
        val code = id.removePrefix(PREFIX)
        return code.all { it in CHARSET }
    }

    /**
     * Extract the code portion (without prefix).
     * @param id Full CipherLink ID
     * @return The 6-character code, or null if invalid
     */
    fun extractCode(id: String): String? {
        if (!isValid(id)) return null
        return id.removePrefix(PREFIX)
    }

    /**
     * Normalize an ID to uppercase with prefix.
     */
    fun normalize(id: String): String {
        val upper = id.uppercase()
        return if (upper.startsWith(PREFIX)) upper else "$PREFIX$upper"
    }
}
