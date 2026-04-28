package com.fino.app.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Thin wrapper around [BiometricPrompt] / [BiometricManager] for the Fino app.
 *
 * - Authenticators: biometric strong/weak + device credential (PIN/pattern/password)
 *   so the user always has a fallback path to unlock.
 * - Confirmation prompt disabled — single-tap unlock once the finger/face matches.
 */
object BiometricAuthHelper {

    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    enum class Availability {
        AVAILABLE,
        NO_HARDWARE,
        HW_UNAVAILABLE,
        NONE_ENROLLED,
        UNSUPPORTED
    }

    fun availability(context: Context): Availability {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Availability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Availability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Availability.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Availability.NONE_ENROLLED
            else -> Availability.UNSUPPORTED
        }
    }

    fun prompt(
        activity: FragmentActivity,
        title: String = "Unlock Fino",
        subtitle: String = "Confirm it's you to continue",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, message: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                onFailed()
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .setConfirmationRequired(false)
            .build()

        prompt.authenticate(promptInfo)
    }
}
