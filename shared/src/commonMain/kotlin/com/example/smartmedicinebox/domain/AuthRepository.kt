package com.example.smartmedicinebox.domain

import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.data.Session
import com.example.smartmedicinebox.data.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.time.Clock

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}

/**
 * Accounts stored on this phone: sign up, log in, log out.
 * The login is remembered in the database, so the app stays logged in
 * after it is closed (and after an Android backup restore).
 */
class AuthRepository(
    private val database: AppDatabase,
    /** Password hashing is slow on purpose; keep it off the main thread. */
    private val hashingDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val users get() = database.userDao()

    /** The logged-in user, or null. */
    val currentUser: Flow<User?> = users.observeLoggedInUser()

    suspend fun signUp(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): AuthResult {
        val cleanEmail = AccountRules.normalizeEmail(email)
        val cleanUsername = username.trim()

        AccountRules.emailError(cleanEmail)?.let { return AuthResult.Error(it) }
        AccountRules.usernameError(cleanUsername)?.let { return AuthResult.Error(it) }
        AccountRules.passwordError(password)?.let { return AuthResult.Error(it) }
        if (password != confirmPassword) {
            return AuthResult.Error("Les mots de passe ne correspondent pas.")
        }

        if (users.findByEmail(cleanEmail) != null) {
            return AuthResult.Error("Un compte existe déjà avec cette adresse e-mail.")
        }
        if (users.findByUsername(cleanUsername) != null) {
            return AuthResult.Error("Ce nom d'utilisateur est déjà pris.")
        }

        val hashed = withContext(hashingDispatcher) { PasswordHasher.hash(password) }
        val now = Clock.System.now().toEpochMilliseconds()
        val user = User(
            email = cleanEmail,
            username = cleanUsername,
            passwordHash = hashed.hashHex,
            passwordSalt = hashed.saltHex,
            passwordIterations = hashed.iterations,
            createdAt = now
        )

        val id = try {
            users.insert(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Unique index hit (e.g. two sign-ups at the same moment).
            return AuthResult.Error("Ce compte existe déjà.")
        }

        val saved = user.copy(id = id)
        users.setSession(Session(userId = saved.id, loggedInAt = now))
        return AuthResult.Success(saved)
    }

    /** [identifier] is the email address or the username. */
    suspend fun logIn(identifier: String, password: String): AuthResult {
        val id = identifier.trim()
        if (id.isEmpty() || password.isEmpty()) {
            return AuthResult.Error("Veuillez saisir votre identifiant et votre mot de passe.")
        }

        val user = if ('@' in id) {
            users.findByEmail(AccountRules.normalizeEmail(id))
        } else {
            users.findByUsername(id)
        }

        val ok = withContext(hashingDispatcher) {
            if (user == null) {
                // Same amount of work as a real check, so the response time
                // does not reveal whether the account exists.
                PasswordHasher.verify(password, DUMMY_HASH, DUMMY_SALT, PasswordHasher.ITERATIONS)
                false
            } else {
                PasswordHasher.verify(
                    password, user.passwordHash, user.passwordSalt, user.passwordIterations
                )
            }
        }

        if (!ok || user == null) {
            return AuthResult.Error("Identifiant ou mot de passe incorrect.")
        }

        users.setSession(
            Session(userId = user.id, loggedInAt = Clock.System.now().toEpochMilliseconds())
        )
        return AuthResult.Success(user)
    }

    suspend fun logOut() {
        users.clearSession()
    }

    private companion object {
        const val DUMMY_HASH = "0000000000000000000000000000000000000000000000000000000000000000"
        const val DUMMY_SALT = "00000000000000000000000000000000"
    }
}
