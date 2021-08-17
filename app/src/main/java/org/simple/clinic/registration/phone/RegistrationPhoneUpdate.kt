package org.simple.clinic.registration.phone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.user.UserStatus
import java.util.UUID
import org.simple.clinic.registration.phone.FacilitiesSynced.Result as FacilitySyncResult
import org.simple.clinic.registration.phone.SearchForExistingUserCompleted.Result as SearchUserResult

class RegistrationPhoneUpdate : Update<RegistrationPhoneModel, RegistrationPhoneEvent, RegistrationPhoneEffect> {

  override fun update(
      model: RegistrationPhoneModel,
      event: RegistrationPhoneEvent
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return when (event) {
      is RegistrationPhoneNumberTextChanged -> next(model.phoneNumberChanged(event.phoneNumber))
      is RegistrationPhoneDoneClicked -> dispatch(ValidateEnteredNumber(model.ongoingRegistrationEntry.phoneNumber!!))
      is EnteredNumberValidated -> syncFacilitiesOnValidNumber(model, event)
      is FacilitiesSynced -> lookupUserByNumber(event, model)
      is SearchForExistingUserCompleted -> registerOrLoginUser(model, event.result)
      is UserCreatedLocally -> next(model.switchToPhoneEntryMode(), ProceedToLogin)
      is CurrentUserUnauthorizedStatusLoaded -> showUserLoggedOut(event)
    }
  }

  private fun showUserLoggedOut(event: CurrentUserUnauthorizedStatusLoaded): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return if (event.isUserUnauthorized) {
      dispatch(ShowUserLoggedOutAlert)
    } else {
      noChange()
    }
  }

  private fun syncFacilitiesOnValidNumber(
      model: RegistrationPhoneModel,
      event: EnteredNumberValidated
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    val updatedModel = model.phoneNumberValidated(event.result)

    return if (updatedModel.isEnteredNumberValid)
      next(updatedModel.clearPhoneRegistrationResult().switchToProgressMode(), SyncFacilities as RegistrationPhoneEffect)
    else
      next(updatedModel)
  }

  private fun lookupUserByNumber(
      event: FacilitiesSynced,
      model: RegistrationPhoneModel
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return when (event.result) {
      FacilitySyncResult.Synced -> dispatch(SearchForExistingUser(model.ongoingRegistrationEntry!!.phoneNumber!!) as RegistrationPhoneEffect)
      FacilitySyncResult.NetworkError -> next(model.switchToPhoneEntryMode().withRegistrationResult(RegistrationResult.NetworkError))
      FacilitySyncResult.OtherError -> next(model.switchToPhoneEntryMode().withRegistrationResult(RegistrationResult.OtherError))
    }
  }

  private fun registerOrLoginUser(
      model: RegistrationPhoneModel,
      result: SearchForExistingUserCompleted.Result
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return when (result) {
      is SearchUserResult.Found -> saveFoundUserLocally(model, result.uuid, result.status)
      SearchUserResult.NotFound -> next(model.switchToPhoneEntryMode(), ContinueRegistration(model.ongoingRegistrationEntry))
      SearchUserResult.NetworkError -> next(model.switchToPhoneEntryMode().withRegistrationResult(RegistrationResult.NetworkError))
      SearchUserResult.OtherError -> next(model.switchToPhoneEntryMode().withRegistrationResult(RegistrationResult.OtherError))
    }
  }

  private fun saveFoundUserLocally(
      model: RegistrationPhoneModel,
      foundUserUuid: UUID,
      foundUserStatus: UserStatus
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    val number = model.ongoingRegistrationEntry!!.phoneNumber!!

    return if (foundUserStatus is UserStatus.DisapprovedForSyncing)
      next(model.switchToPhoneEntryMode(), ShowAccessDeniedScreen(number) as RegistrationPhoneEffect)
    else
      dispatch(CreateUserLocally(foundUserUuid, number, foundUserStatus) as RegistrationPhoneEffect)
  }
}
