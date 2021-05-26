package org.simple.clinic.registration.register

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class RegistrationLoadingUpdate : Update<RegistrationLoadingModel, RegistrationLoadingEvent, RegistrationLoadingEffect> {

  override fun update(
      model: RegistrationLoadingModel,
      event: RegistrationLoadingEvent
  ): Next<RegistrationLoadingModel, RegistrationLoadingEffect> {
    return when (event) {
      is UserRegistrationCompleted -> userRegistrationCompleted(event, model)
      is RegisterErrorRetryClicked -> next(model.clearRegistrationResult(), ConvertRegistrationEntryToUserDetails(model.registrationEntry))
      is ConvertedRegistrationEntryToUserDetails -> dispatch(RegisterUserAtFacility(event.user))
    }
  }

  private fun userRegistrationCompleted(
      event: UserRegistrationCompleted,
      model: RegistrationLoadingModel
  ): Next<RegistrationLoadingModel, RegistrationLoadingEffect> {
    val result = event.result

    return if (result == RegisterUserResult.Success) {
      dispatch(GoToHomeScreen as RegistrationLoadingEffect)
    } else {
      next(model.withRegistrationResult(result))
    }
  }
}
