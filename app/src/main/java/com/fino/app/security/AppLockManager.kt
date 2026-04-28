package com.fino.app.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds app-wide lock state. MainActivity observes this to decide whether to render
 * the main navigation or the lock screen overlay.
 *
 * The app starts locked if biometric is enabled; it's locked again whenever the
 * activity leaves the foreground.
 */
@Singleton
class AppLockManager @Inject constructor() {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    fun lock() {
        _isLocked.value = true
    }

    fun unlock() {
        _isLocked.value = false
    }
}
