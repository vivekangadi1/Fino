package com.fino.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fino_settings")

data class SettingsData(
    val notifyUncategorized: Boolean = true,
    val notifyRecurringPatterns: Boolean = true,
    val notifyBillReminders: Boolean = true,
    val notifyEMIDue: Boolean = true,
    val dailyDigestEnabled: Boolean = true,
    val dailyDigestHour: Int = 21, // 9 PM
    val dailyDigestMinute: Int = 0,
    val darkModeEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val currencySymbol: String = "₹",
    val defaultPaymentMethod: String = "UPI"
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val NOTIFY_UNCATEGORIZED = booleanPreferencesKey("notify_uncategorized")
        val NOTIFY_RECURRING_PATTERNS = booleanPreferencesKey("notify_recurring_patterns")
        val NOTIFY_BILL_REMINDERS = booleanPreferencesKey("notify_bill_reminders")
        val NOTIFY_EMI_DUE = booleanPreferencesKey("notify_emi_due")
        val DAILY_DIGEST_ENABLED = booleanPreferencesKey("daily_digest_enabled")
        val DAILY_DIGEST_HOUR = intPreferencesKey("daily_digest_hour")
        val DAILY_DIGEST_MINUTE = intPreferencesKey("daily_digest_minute")
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val DEFAULT_PAYMENT_METHOD = stringPreferencesKey("default_payment_method")
    }

    val settingsFlow: Flow<SettingsData> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            SettingsData(
                notifyUncategorized = preferences[PreferencesKeys.NOTIFY_UNCATEGORIZED] ?: true,
                notifyRecurringPatterns = preferences[PreferencesKeys.NOTIFY_RECURRING_PATTERNS] ?: true,
                notifyBillReminders = preferences[PreferencesKeys.NOTIFY_BILL_REMINDERS] ?: true,
                notifyEMIDue = preferences[PreferencesKeys.NOTIFY_EMI_DUE] ?: true,
                dailyDigestEnabled = preferences[PreferencesKeys.DAILY_DIGEST_ENABLED] ?: true,
                dailyDigestHour = preferences[PreferencesKeys.DAILY_DIGEST_HOUR] ?: 21,
                dailyDigestMinute = preferences[PreferencesKeys.DAILY_DIGEST_MINUTE] ?: 0,
                darkModeEnabled = preferences[PreferencesKeys.DARK_MODE_ENABLED] ?: true,
                biometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
                currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: "₹",
                defaultPaymentMethod = preferences[PreferencesKeys.DEFAULT_PAYMENT_METHOD] ?: "UPI"
            )
        }

    suspend fun setNotifyUncategorized(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_UNCATEGORIZED] = enabled
        }
    }

    suspend fun setNotifyRecurringPatterns(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_RECURRING_PATTERNS] = enabled
        }
    }

    suspend fun setNotifyBillReminders(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_BILL_REMINDERS] = enabled
        }
    }

    suspend fun setNotifyEMIDue(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_EMI_DUE] = enabled
        }
    }

    suspend fun setDailyDigestEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_DIGEST_ENABLED] = enabled
        }
    }

    suspend fun setDailyDigestTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_DIGEST_HOUR] = hour
            preferences[PreferencesKeys.DAILY_DIGEST_MINUTE] = minute
        }
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun setDefaultPaymentMethod(method: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_PAYMENT_METHOD] = method
        }
    }
}
