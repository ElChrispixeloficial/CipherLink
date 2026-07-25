package com.chris.chipherlink.integrity

/**
 * Represents the result of an integrity verification check.
 */
sealed class IntegrityStatus {
    /** All protected files match their stored fingerprints. */
    data object Valid : IntegrityStatus()

    /** One or more files were modified, corrupted, or are missing. */
    data class Tampered(
        val compromisedFiles: List<String>,
        val message: String = "Se detectó una modificación en los datos de identidad " +
                "o almacenamiento local. Revisa la seguridad de tu dispositivo."
    ) : IntegrityStatus()

    /** No identity exists yet — first launch or after logout. */
    data object NoIdentity : IntegrityStatus()

    /** An error occurred during verification (I/O, crypto). */
    data class Error(val exception: Exception) : IntegrityStatus()
}
