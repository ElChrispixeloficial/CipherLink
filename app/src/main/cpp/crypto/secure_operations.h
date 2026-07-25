#ifndef SECURE_OPERATIONS_H
#define SECURE_OPERATIONS_H

#include <string>
#include <cstdint>
#include <cstddef>

namespace secure_ops {

/**
 * Calculate SHA-256 hash of input data.
 * Uses OpenSSL's EVP interface when available, falls back to Android's.
 */
std::string sha256_hash(const uint8_t* data, size_t length);

/**
 * Calculate HMAC-SHA256 of data using provided key.
 */
std::string hmac_sha256(
    const uint8_t* data, size_t data_length,
    const uint8_t* key, size_t key_length);

/**
 * Constant-time comparison of two byte arrays.
 * Prevents timing attacks on hash/token verification.
 */
bool constant_time_compare(
    const void* a, size_t a_len,
    const void* b, size_t b_len);

/**
 * Base64 encode binary data.
 */
std::string base64_encode(const uint8_t* data, size_t length);

/**
 * Base64 decode string to binary data.
 */
std::string base64_decode(const std::string& encoded);

} // namespace secure_ops

#endif // SECURE_OPERATIONS_H
