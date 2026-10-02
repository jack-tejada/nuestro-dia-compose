package com.madrigalsolu.nuestrodia.compose.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.madrigalsolu.nuestrodia.compose.feature.auth.data.AuthRepository
import com.madrigalsolu.nuestrodia.compose.feature.auth.data.AuthUser
import com.madrigalsolu.nuestrodia.compose.feature.auth.data.MissingFirebaseConfigurationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
  val isLoading: Boolean = false,
  val user: AuthUser? = null,
  val errorMessage: String? = null,
  val notice: String? = null,
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
  private val _uiState = MutableStateFlow(AuthUiState(user = repository.currentUser()))
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  fun signIn(email: String, password: String) {
    if (email.isBlank() || password.isBlank()) {
      showError("Ingresá tu correo y contraseña para continuar.")
      return
    }
    runRequest { repository.signIn(email, password) }
  }

  fun register(fullName: String, email: String, password: String, confirmPassword: String, acceptedTerms: Boolean) {
    val validation = validateRegistration(fullName, email, password, confirmPassword, acceptedTerms)
    if (validation != null) {
      showError(validation)
      return
    }
    runRequest { repository.register(fullName, email, password) }
  }

  fun signOut() {
    runCatching { repository.signOut() }.onFailure(::setFailure)
    _uiState.update { it.copy(user = null, errorMessage = null, notice = null) }
  }

  fun resetPassword(email: String) {
    val validation = validateEmail(email)
    if (validation != null) {
      showError(validation)
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null, notice = null) }
      runCatching { repository.sendPasswordReset(email) }
        .onSuccess { _uiState.update { it.copy(isLoading = false, notice = "Si el correo está registrado, vas a recibir un enlace para restablecer tu contraseña.") } }
        .onFailure(::setFailure)
    }
  }

  fun clearFeedback() = _uiState.update { it.copy(errorMessage = null, notice = null) }

  private fun runRequest(request: suspend () -> AuthUser) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null, notice = null) }
      runCatching { request() }
        .onSuccess { user -> _uiState.update { it.copy(isLoading = false, user = user) } }
        .onFailure(::setFailure)
    }
  }

  private fun setFailure(error: Throwable) {
    val message = when (error) {
      is MissingFirebaseConfigurationException -> error.message.orEmpty()
      is FirebaseAuthException -> when (error.errorCode) {
        "ERROR_INVALID_EMAIL" -> "Revisá el formato del correo electrónico."
        "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "El correo o la contraseña no coinciden."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo."
        "ERROR_WEAK_PASSWORD" -> "Elegí una contraseña más segura."
        "ERROR_TOO_MANY_REQUESTS" -> "Hubo demasiados intentos. Esperá un momento y probá de nuevo."
        else -> "No pudimos completar la solicitud. Revisá tu conexión e intentá de nuevo."
      }
      else -> "No pudimos completar la solicitud. Revisá tu conexión e intentá de nuevo."
    }
    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
  }

  private fun showError(message: String) = _uiState.update { it.copy(errorMessage = message, notice = null) }
}

fun validateRegistration(fullName: String, email: String, password: String, confirmPassword: String, acceptedTerms: Boolean): String? = when {
  fullName.trim().length < 2 -> "Ingresá tu nombre completo."
  validateEmail(email) != null -> "Ingresá un correo electrónico válido."
  password.length < 8 -> "Usá al menos 8 caracteres."
  password.none(Char::isDigit) -> "Sumá al menos un número."
  password.none { !it.isLetterOrDigit() } -> "Sumá al menos un carácter especial."
  password != confirmPassword -> "Las contraseñas no coinciden."
  !acceptedTerms -> "Para crear tu cuenta, necesitás aceptar las condiciones de uso."
  else -> null
}

fun validateEmail(email: String): String? =
  if (EMAIL_PATTERN.matches(email.trim())) null else "Ingresá un correo electrónico válido."

private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
