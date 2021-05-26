package org.simple.clinic.login.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class LoginPinUpdate : Update<LoginPinModel, LoginPinEvent, LoginPinEffect> {
  override fun update(
      model: LoginPinModel,
      event: LoginPinEvent
  ): Next<LoginPinModel, LoginPinEffect> {
    return when (event) {
      is OngoingLoginEntryLoaded -> next(
          model.ongoingLoginEntryUpdated(event.ongoingLoginEntry)
      )
      is LoginPinAuthenticated -> next(
          model.ongoingLoginEntryUpdated(event.newLoginEntry),
          SaveOngoingLoginEntry(event.newLoginEntry)
      )
      is LoginPinScreenUpdatedLoginEntry -> dispatch(
          LoginUser(event.ongoingLoginEntry)
      )
      UserLoggedIn -> dispatch(OpenHomeScreen)
      OngoingLoginEntryCleared -> dispatch(GoBackToRegistrationScreen)
      PinBackClicked -> dispatch(ClearOngoingLoginEntry)
    }
  }
}
