package com.fino.app.service.gmail

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.fino.app.service.security.SecureTokenStore
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps GoogleSignInClient with the `gmail.readonly` scope. Callers drive the
 * sign-in intent through a registered ActivityResultLauncher; this class is
 * not bound to any one activity.
 *
 * Tokens are fetched via GoogleAuthUtil.getToken on a worker thread and cached
 * in SecureTokenStore. Callers should treat cached tokens as short-lived and
 * re-fetch on API 401.
 */
@Singleton
class GmailAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureTokenStore: SecureTokenStore
) {

    private val signInClient: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(GmailScopes.GMAIL_READONLY))
            .build()
        GoogleSignIn.getClient(context, options)
    }

    fun getConnectedEmail(): String? =
        GoogleSignIn.getLastSignedInAccount(context)?.email
            ?: secureTokenStore.getString(SecureTokenStore.KEY_GMAIL_ACCOUNT_EMAIL)

    fun isConnected(): Boolean = getConnectedEmail() != null

    /**
     * Register a launcher in the hosting Activity's onCreate; invoke the
     * returned lambda to kick off the consent UI.
     */
    fun buildSignInLauncher(
        activity: ComponentActivity,
        onResult: (Result<String>) -> Unit
    ): () -> Unit {
        val launcher: ActivityResultLauncher<Intent> =
            activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                handleSignInResult(result.data, onResult)
            }
        return { launcher.launch(signInClient.signInIntent) }
    }

    private fun handleSignInResult(data: Intent?, onResult: (Result<String>) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(Exception::class.java)
            if (account?.email == null) {
                onResult(Result.failure(IllegalStateException("No email on signed-in account")))
                return
            }
            secureTokenStore.putString(
                SecureTokenStore.KEY_GMAIL_ACCOUNT_EMAIL,
                account.email
            )
            onResult(Result.success(account.email!!))
        } catch (e: Exception) {
            onResult(Result.failure(e))
        }
    }

    /**
     * Fetch a fresh OAuth access token. Must be called off the main thread.
     * Uses the cached email to resolve the signed-in account. Returns null
     * if no account is connected or the token call failed.
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val account: GoogleSignInAccount =
            GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
        val accountName = account.account ?: return@withContext null
        val scope = "oauth2:${GmailScopes.GMAIL_READONLY}"
        try {
            val token = GoogleAuthUtil.getToken(context, accountName, scope)
            secureTokenStore.putString(SecureTokenStore.KEY_GMAIL_ACCESS_TOKEN, token)
            token
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        runCatching { signInClient.signOut() }
        runCatching { signInClient.revokeAccess() }
        secureTokenStore.clear(SecureTokenStore.KEY_GMAIL_ACCOUNT_EMAIL)
        secureTokenStore.clear(SecureTokenStore.KEY_GMAIL_ACCESS_TOKEN)
    }
}
