package com.madrigalsolu.nuestrodia.compose.feature.auth.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

data class AuthUser(val id: String = "", val displayName: String? = null)

interface AuthRepository {
  fun currentUser(): AuthUser?
  fun signOut()
  suspend fun signIn(email: String, password: String): AuthUser
  suspend fun register(fullName: String, email: String, password: String): AuthUser
  suspend fun sendPasswordReset(email: String)
}

class MissingFirebaseConfigurationException : IllegalStateException(
  "Esta app todavía no está conectada a Firebase. Registrá Nuestro Día Compose en Firebase, agregá su google-services.json y habilitá Email/Password.",
)

class FirebaseAuthRepository(context: Context) : AuthRepository {
  private val applicationContext = context.applicationContext

  private val firebaseAuth: FirebaseAuth
    get() {
      if (FirebaseApp.getApps(applicationContext).isEmpty()) {
        throw MissingFirebaseConfigurationException()
      }
      return FirebaseAuth.getInstance()
    }

  override fun currentUser(): AuthUser? = runCatching { firebaseAuth.currentUser?.toAuthUser() }.getOrNull()

  override fun signOut() = firebaseAuth.signOut()

  override suspend fun signIn(email: String, password: String): AuthUser =
    firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await().user?.toAuthUser()
      ?: throw IllegalStateException("No se pudo iniciar la sesión. Intentá de nuevo.")

  override suspend fun register(fullName: String, email: String, password: String): AuthUser {
    val user = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await().user
      ?: throw IllegalStateException("No se pudo crear la cuenta. Intentá de nuevo.")
    user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName.trim()).build()).await()
    return user.toAuthUser()
  }

  override suspend fun sendPasswordReset(email: String) {
    firebaseAuth.sendPasswordResetEmail(email.trim()).await()
  }

  private fun com.google.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(uid, displayName)
}
