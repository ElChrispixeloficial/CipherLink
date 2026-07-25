#ifndef CIPHERCORE_H
#define CIPHERCORE_H

#include <jni.h>
#include <string>
#include <cstdint>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * CipherLink Native Security Core
 *
 * This module provides auxiliary cryptographic operations,
 * integrity validations, and secure hashing.
 *
 * Security Rules:
 * - No private keys stored in C++ code
 * - No hardcoded secrets
 * - Master keys remain in Android Keystore
 * - C++ complements, does not replace, Kotlin crypto APIs
 */

JNIEXPORT jstring JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeCalculateSecureHash(
    JNIEnv *env, jobject thiz, jbyteArray data);

JNIEXPORT jstring JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeCalculateHmac(
    JNIEnv *env, jobject thiz, jbyteArray data, jbyteArray key);

JNIEXPORT jboolean JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeConstantTimeCompare(
    JNIEnv *env, jobject thiz, jbyteArray a, jbyteArray b);

JNIEXPORT jint JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeHealthCheck(
    JNIEnv *env, jobject thiz);

JNIEXPORT jstring JNICALL
Java_com_chris_chipherlink_security_NativeSecurityBridge_nativeGetVersion(
    JNIEnv *env, jobject thiz);

#ifdef __cplusplus
}
#endif

#endif // CIPHERCORE_H
