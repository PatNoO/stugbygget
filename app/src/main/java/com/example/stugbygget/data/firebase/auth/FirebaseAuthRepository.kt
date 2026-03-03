package com.example.stugbygget.data.firebase.auth

import com.example.stugbygget.domain.model.AppUser
import com.example.stugbygget.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun observeCurrentUser(): Flow<AppUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            trySend(
                user?.let {
                    AppUser(
                        uid = it.uid,
                        displayName = it.displayName,
                        email = it.email
                    )
                }
            )
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmailPassword(email: String, password: String) {
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
        } catch (throwable: Throwable) {
            throw mapSignInError(throwable)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun mapSignInError(throwable: Throwable): Throwable {
        return when (throwable) {
            is FirebaseAuthInvalidCredentialsException -> {
                IllegalArgumentException("Incorrect email or password.")
            }

            is FirebaseAuthInvalidUserException -> {
                IllegalArgumentException("No account found for this email.")
            }

            is FirebaseAuthException -> {
                when (throwable.errorCode) {
                    "ERROR_USER_DISABLED" -> IllegalArgumentException("This account has been disabled.")
                    "ERROR_TOO_MANY_REQUESTS" -> IllegalStateException("Too many attempts. Try again later.")
                    else -> IllegalStateException("Could not sign in right now. Please try again.")
                }
            }

            else -> IllegalStateException("Could not sign in right now. Please try again.")
        }
    }
}
