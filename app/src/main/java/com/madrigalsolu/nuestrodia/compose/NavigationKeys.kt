package com.madrigalsolu.nuestrodia.compose

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SignInKey : NavKey
@Serializable data object RegisterKey : NavKey
@Serializable data object ResetPasswordKey : NavKey
@Serializable data object TermsKey : NavKey
@Serializable data object ProfileKey : NavKey
@Serializable data class GalleryKey(val eventId: String = "boda-principal") : NavKey
@Serializable data class CapturePreviewKey(val uriString: String, val eventId: String = "boda-principal") : NavKey
@Serializable data class TvDisplayKey(val eventId: String = "boda-principal") : NavKey
