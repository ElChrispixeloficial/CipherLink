#include "secure_operations.h"
#include <cstring>
#include <android/log.h>

#define LOG_TAG "SecureOps"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace secure_ops {

// SHA-256 constants
static const uint32_t SHA256_K[64] = {
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
    0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
    0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
    0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
    0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
    0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
    0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
    0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
    0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
    0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
    0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};

static inline uint32_t rotr(uint32_t x, int n) {
    return (x >> n) | (x << (32 - n));
}

static inline uint32_t ch(uint32_t x, uint32_t y, uint32_t z) {
    return (x & y) ^ (~x & z);
}

static inline uint32_t maj(uint32_t x, uint32_t y, uint32_t z) {
    return (x & y) ^ (x & z) ^ (y & z);
}

static inline uint32_t sigma0(uint32_t x) {
    return rotr(x, 2) ^ rotr(x, 13) ^ rotr(x, 22);
}

static inline uint32_t sigma1(uint32_t x) {
    return rotr(x, 6) ^ rotr(x, 11) ^ rotr(x, 25);
}

static inline uint32_t gamma0(uint32_t x) {
    return rotr(x, 7) ^ rotr(x, 18) ^ (x >> 3);
}

static inline uint32_t gamma1(uint32_t x) {
    return rotr(x, 17) ^ rotr(x, 19) ^ (x >> 10);
}

static void sha256_transform(uint32_t state[8], const uint8_t block[64]) {
    uint32_t W[64];
    uint32_t a, b, c, d, e, f, g, h, t1, t2;
    
    for (int t = 0; t < 16; t++) {
        W[t] = (uint32_t)block[t * 4] << 24 |
               (uint32_t)block[t * 4 + 1] << 16 |
               (uint32_t)block[t * 4 + 2] << 8 |
               (uint32_t)block[t * 4 + 3];
    }
    
    for (int t = 16; t < 64; t++) {
        W[t] = gamma1(W[t - 2]) + W[t - 7] + gamma0(W[t - 15]) + W[t - 16];
    }
    
    a = state[0]; b = state[1]; c = state[2]; d = state[3];
    e = state[4]; f = state[5]; g = state[6]; h = state[7];
    
    for (int t = 0; t < 64; t++) {
        t1 = h + sigma1(e) + ch(e, f, g) + SHA256_K[t] + W[t];
        t2 = sigma0(a) + maj(a, b, c);
        h = g; g = f; f = e; e = d + t1;
        d = c; c = b; b = a; a = t1 + t2;
    }
    
    state[0] += a; state[1] += b; state[2] += c; state[3] += d;
    state[4] += e; state[5] += f; state[6] += g; state[7] += h;
}

std::string sha256_hash(const uint8_t* data, size_t length) {
    uint32_t state[8] = {
        0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
        0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    };
    
    uint8_t buffer[64];
    size_t buffer_len = 0;
    uint64_t total_len = length;
    
    // Process full blocks
    for (size_t i = 0; i + 64 <= length; i += 64) {
        sha256_transform(state, data + i);
    }
    
    // Handle remaining bytes
    size_t remaining = length % 64;
    if (remaining > 0) {
        memcpy(buffer, data + (length - remaining), remaining);
        buffer_len = remaining;
    }
    
    // Padding
    buffer[buffer_len++] = 0x80;
    
    if (buffer_len > 56) {
        memset(buffer + buffer_len, 0, 64 - buffer_len);
        sha256_transform(state, buffer);
        buffer_len = 0;
    }
    
    memset(buffer + buffer_len, 0, 56 - buffer_len);
    
    // Append length in bits
    total_len *= 8;
    buffer[56] = (total_len >> 56) & 0xff;
    buffer[57] = (total_len >> 48) & 0xff;
    buffer[58] = (total_len >> 40) & 0xff;
    buffer[59] = (total_len >> 32) & 0xff;
    buffer[60] = (total_len >> 24) & 0xff;
    buffer[61] = (total_len >> 16) & 0xff;
    buffer[62] = (total_len >> 8) & 0xff;
    buffer[63] = total_len & 0xff;
    
    sha256_transform(state, buffer);
    
    // Produce hash
    std::string result;
    result.resize(32);
    for (int i = 0; i < 8; i++) {
        result[i * 4] = (state[i] >> 24) & 0xff;
        result[i * 4 + 1] = (state[i] >> 16) & 0xff;
        result[i * 4 + 2] = (state[i] >> 8) & 0xff;
        result[i * 4 + 3] = state[i] & 0xff;
    }
    
    return result;
}

std::string hmac_sha256(
    const uint8_t* data, size_t data_length,
    const uint8_t* key, size_t key_length) {
    
    const size_t BLOCK_SIZE = 64;
    const size_t HASH_SIZE = 32;
    
    uint8_t key_padded[BLOCK_SIZE];
    memset(key_padded, 0, BLOCK_SIZE);
    
    if (key_length > BLOCK_SIZE) {
        std::string key_hash = sha256_hash(key, key_length);
        memcpy(key_padded, key_hash.data(), HASH_SIZE);
    } else {
        memcpy(key_padded, key, key_length);
    }
    
    // Inner padding
    uint8_t ipad[BLOCK_SIZE];
    for (size_t i = 0; i < BLOCK_SIZE; i++) {
        ipad[i] = key_padded[i] ^ 0x36;
    }
    
    // Outer padding
    uint8_t opad[BLOCK_SIZE];
    for (size_t i = 0; i < BLOCK_SIZE; i++) {
        opad[i] = key_padded[i] ^ 0x5c;
    }
    
    // Inner hash: H(K XOR ipad || message)
    std::string inner;
    inner.append(reinterpret_cast<const char*>(ipad), BLOCK_SIZE);
    inner.append(reinterpret_cast<const char*>(data), data_length);
    
    std::string inner_hash = sha256_hash(
        reinterpret_cast<const uint8_t*>(inner.data()), inner.size());
    
    // Outer hash: H(K XOR opad || inner_hash)
    std::string outer;
    outer.append(reinterpret_cast<const char*>(opad), BLOCK_SIZE);
    outer.append(inner_hash);
    
    return sha256_hash(
        reinterpret_cast<const uint8_t*>(outer.data()), outer.size());
}

bool constant_time_compare(
    const void* a, size_t a_len,
    const void* b, size_t b_len) {
    
    if (a_len != b_len) {
        return false;
    }
    
    const volatile uint8_t* x = (const volatile uint8_t*)a;
    const volatile uint8_t* y = (const volatile uint8_t*)b;
    
    volatile uint8_t result = 0;
    
    for (size_t i = 0; i < a_len; i++) {
        result |= x[i] ^ y[i];
    }
    
    return result == 0;
}

static const char BASE64_TABLE[] = 
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

std::string base64_encode(const uint8_t* data, size_t length) {
    std::string result;
    result.reserve(((length + 2) / 3) * 4);
    
    for (size_t i = 0; i < length; i += 3) {
        uint32_t n = (uint32_t)data[i] << 16;
        
        if (i + 1 < length) n |= (uint32_t)data[i + 1] << 8;
        if (i + 2 < length) n |= (uint32_t)data[i + 2];
        
        result += BASE64_TABLE[(n >> 18) & 0x3F];
        result += BASE64_TABLE[(n >> 12) & 0x3F];
        
        if (i + 1 < length) result += BASE64_TABLE[(n >> 6) & 0x3F];
        else result += '=';
        
        if (i + 2 < length) result += BASE64_TABLE[n & 0x3F];
        else result += '=';
    }
    
    return result;
}

std::string base64_decode(const std::string& encoded) {
    static const uint8_t DECODE_TABLE[256] = {
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,62,64,64,64,63,
        52,53,54,55,56,57,58,59,60,61,64,64,64,64,64,64,
        64, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,
        15,16,17,18,19,20,21,22,23,24,25,64,64,64,64,64,
        64,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
        41,42,43,44,45,46,47,48,49,50,51,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,
        64,64,64,64,64,64,64,64,64,64,64,64,64,64,64,64
    };
    
    std::string result;
    result.reserve(encoded.size() * 3 / 4);
    
    uint32_t buffer = 0;
    int bits = 0;
    
    for (char c : encoded) {
        if (c == '=') break;
        
        uint8_t val = DECODE_TABLE[(uint8_t)c];
        if (val >= 64) continue;
        
        buffer = (buffer << 6) | val;
        bits += 6;
        
        if (bits >= 8) {
            bits -= 8;
            result += (char)((buffer >> bits) & 0xFF);
        }
    }
    
    return result;
}

} // namespace secure_ops
