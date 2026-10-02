package com.madrigalsolu.nuestrodia.compose.feature.auth.ui

import com.madrigalsolu.nuestrodia.compose.feature.auth.data.AuthRepository
import com.madrigalsolu.nuestrodia.compose.feature.auth.data.AuthUser
import com.madrigalsolu.nuestrodia.compose.feature.auth.data.MissingFirebaseConfigurationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @Before fun setMainDispatcher() = Dispatchers.setMain(dispatcher)
  @After fun resetMainDispatcher() = Dispatchers.resetMain()

  @Test fun registrationRequiresValidNameEmailStrongMatchingPasswordAndConsent() {
    assertEquals("Ingresá tu nombre completo.", validateRegistration(" ", "ana@example.com", "Dulce123!", "Dulce123!", true))
    assertEquals("Ingresá un correo electrónico válido.", validateRegistration("Ana Ruiz", "wrong", "Dulce123!", "Dulce123!", true))
    assertEquals("Usá al menos 8 caracteres.", validateRegistration("Ana Ruiz", "ana@example.com", "Ab1!", "Ab1!", true))
    assertEquals("Sumá al menos un número.", validateRegistration("Ana Ruiz", "ana@example.com", "Dulceeee!", "Dulceeee!", true))
    assertEquals("Sumá al menos un carácter especial.", validateRegistration("Ana Ruiz", "ana@example.com", "Dulce1234", "Dulce1234", true))
    assertEquals("Las contraseñas no coinciden.", validateRegistration("Ana Ruiz", "ana@example.com", "Dulce123!", "Dulce123?", true))
    assertEquals("Para crear tu cuenta, necesitás aceptar las condiciones de uso.", validateRegistration("Ana Ruiz", "ana@example.com", "Dulce123!", "Dulce123!", false))
    assertNull(validateRegistration("Ana Ruiz", "ana@example.com", "Dulce123!", "Dulce123!", true))
  }

  @Test fun signInExposesLoadingThenAuthenticatedUser() = runTest(dispatcher) {
    val expected = AuthUser("user-1", "Ana Ruiz")
    val viewModel = AuthViewModel(FakeAuthRepository(signInResult = expected))
    viewModel.signIn("ana@example.com", "Dulce123!")
    advanceUntilIdle()
    assertEquals(expected, viewModel.uiState.value.user)
    assertEquals(false, viewModel.uiState.value.isLoading)
    assertNull(viewModel.uiState.value.errorMessage)
  }

  @Test fun missingFirebaseConfigurationBecomesActionableStateInsteadOfCrash() = runTest(dispatcher) {
    val viewModel = AuthViewModel(FakeAuthRepository(signInFailure = MissingFirebaseConfigurationException()))
    viewModel.signIn("ana@example.com", "Dulce123!")
    advanceUntilIdle()
    assertEquals(false, viewModel.uiState.value.isLoading)
    assertEquals("Esta app todavía no está conectada a Firebase. Registrá Nuestro Día Compose en Firebase, agregá su google-services.json y habilitá Email/Password.", viewModel.uiState.value.errorMessage)
  }

  @Test fun passwordResetUsesNonEnumeratingSuccessNotice() = runTest(dispatcher) {
    val viewModel = AuthViewModel(FakeAuthRepository())
    viewModel.resetPassword("ana@example.com")
    advanceUntilIdle()
    assertEquals("Si el correo está registrado, vas a recibir un enlace para restablecer tu contraseña.", viewModel.uiState.first().notice)
  }
}

private class FakeAuthRepository(
  private val signInResult: AuthUser = AuthUser("user-1", "Ana Ruiz"),
  private val signInFailure: Throwable? = null,
) : AuthRepository {
  override fun currentUser(): AuthUser? = null
  override fun signOut() = Unit
  override suspend fun signIn(email: String, password: String): AuthUser = signInFailure?.let { throw it } ?: signInResult
  override suspend fun register(fullName: String, email: String, password: String) = AuthUser("user-2", fullName)
  override suspend fun sendPasswordReset(email: String) = Unit
}
