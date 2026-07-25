#include "ciphercore.h"
#include "crypto/secure_operations.h"
#include <android/log.h>
#include <cstring>

#define LOG_TAG "CipherCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static const char* NATIVE_VERSION = "1.0.0";

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeCalculateSecureHash(
    JNIEnv *env, jobject thiz, jbyteArray data) {

    if (data == nullptr) {
        LOGE("nativeCalculateSecureHash: null data");
        return env->NewStringUTF("");
    }

    jsize length = env->GetArrayLength(data);
    jbyte* dataPtr = env->GetByteArrayElements(data, nullptr);

    if (dataPtr == nullptr) {
        LOGE("nativeCalculateSecureHash: failed to get byte array");
        return env->NewStringUTF("");
    }

    std::string hash = secure_ops::sha256_hash(
        reinterpret_cast<const uint8_t*>(dataPtr), length);

    env->ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);

    std::string base64_hash = secure_ops::base64_encode(
        reinterpret_cast<const uint8_t*>(hash.data()), hash.size());

    return env->NewStringUTF(base64_hash.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeCalculateHmac(
    JNIEnv *env, jobject thiz, jbyteArray data, jbyteArray key) {

    if (data == nullptr || key == nullptr) {
        LOGE("nativeCalculateHmac: null input");
        return env->NewStringUTF("");
    }

    jsize dataLen = env->GetArrayLength(data);
    jsize keyLen = env->GetArrayLength(key);

    jbyte* dataPtr = env->GetByteArrayElements(data, nullptr);
    jbyte* keyPtr = env->GetByteArrayElements(key, nullptr);

    if (dataPtr == nullptr || keyPtr == nullptr) {
        LOGE("nativeCalculateHmac: failed to get byte arrays");
        if (dataPtr) env->ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);
        if (keyPtr) env->ReleaseByteArrayElements(key, keyPtr, JNI_ABORT);
        return env->NewStringUTF("");
    }

    std::string hmac = secure_ops::hmac_sha256(
        reinterpret_cast<const uint8_t*>(dataPtr), dataLen,
        reinterpret_cast<const uint8_t*>(keyPtr), keyLen);

    env->ReleaseByteArrayElements(data, dataPtr, JNI_ABORT);
    env->ReleaseByteArrayElements(key, keyPtr, JNI_ABORT);

    std::string base64_hmac = secure_ops::base64_encode(
        reinterpret_cast<const uint8_t*>(hmac.data()), hmac.size());

    return env->NewStringUTF(base64_hmac.c_str());
}

JNIEXPORT jboolean JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeConstantTimeCompare(
    JNIEnv *env, jobject thiz, jbyteArray a, jbyteArray b) {

    if (a == nullptr || b == nullptr) {
        return JNI_FALSE;
    }

    jsize aLen = env->GetArrayLength(a);
    jsize bLen = env->GetArrayLength(b);

    if (aLen != bLen) {
        return JNI_FALSE;
    }

    jbyte* aPtr = env->GetByteArrayElements(a, nullptr);
    jbyte* bPtr = env->GetByteArrayElements(b, nullptr);

    if (aPtr == nullptr || bPtr == nullptr) {
        if (aPtr) env->ReleaseByteArrayElements(a, aPtr, JNI_ABORT);
        if (bPtr) env->ReleaseByteArrayElements(b, bPtr, JNI_ABORT);
        return JNI_FALSE;
    }

    bool result = secure_ops::constant_time_compare(
        aPtr, aLen, bPtr, bLen);

    env->ReleaseByteArrayElements(a, aPtr, JNI_ABORT);
    env->ReleaseByteArrayElements(b, bPtr, JNI_ABORT);

    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeHealthCheck(
    JNIEnv *env, jobject thiz) {

    LOGI("Native health check started");

    const char* testData = "CipherLink Health Check";
    std::string hash = secure_ops::sha256_hash(
        reinterpret_cast<const uint8_t*>(testData), strlen(testData));

    if (hash.empty()) {
        LOGE("Health check failed: SHA-256 computation");
        return 1;
    }

    std::string encoded = secure_ops::base64_encode(
        reinterpret_cast<const uint8_t*>(hash.data()), hash.size());

    if (encoded.empty()) {
        LOGE("Health check failed: Base64 encoding");
        return 2;
    }

    bool compareResult = secure_ops::constant_time_compare(
        encoded.data(), encoded.size(),
        encoded.data(), encoded.size());

    if (!compareResult) {
        LOGE("Health check failed: Constant-time comparison");
        return 3;
    }

    const char* key = "test_key_12345678";
    std::string hmac = secure_ops::hmac_sha256(
        reinterpret_cast<const uint8_t*>(testData), strlen(testData),
        reinterpret_cast<const uint8_t*>(key), strlen(key));

    if (hmac.empty()) {
        LOGE("Health check failed: HMAC computation");
        return 4;
    }

    LOGI("Native health check passed");
    return 0;
}

JNIEXPORT jstring JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeGetVersion(
    JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(NATIVE_VERSION);
}

} // extern "C"
