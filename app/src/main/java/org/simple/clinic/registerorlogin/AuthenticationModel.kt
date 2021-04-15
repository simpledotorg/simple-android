package org.simple.clinic.registerorlogin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthenticationModel(
    val openFor: OpenFor,
    val openedInitialScreen: Boolean
) : Parcelable {

  companion object {
    fun create(openFor: OpenFor): AuthenticationModel {
      return AuthenticationModel(
          openFor = openFor,
          openedInitialScreen = false
      )
    }
  }

  fun initialScreenOpened(): AuthenticationModel {
    return copy(openedInitialScreen = true)
  }
}
