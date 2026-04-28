package com.fino.app.service.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over EncryptedSharedPreferences for storing sensitive auth
 * material (OAuth tokens, refresh tokens, per-account emails). Never logs
 * token values; callers should avoid doing so as well.
 */
@Singleton
class SecureTokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun putString(key: String, value: String?) {
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    fun getString(key: String): String? = prefs.getString(key, null)

    fun clear(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "fino_secure_tokens"

        const val KEY_GMAIL_ACCOUNT_EMAIL = "gmail_account_email"
        const val KEY_GMAIL_ACCESS_TOKEN = "gmail_access_token"
        const val KEY_GMAIL_LAST_SYNC = "gmail_last_sync"
    }
}
