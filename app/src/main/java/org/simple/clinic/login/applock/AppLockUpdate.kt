package org.simple.clinic.login.applock

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class AppLockUpdate : Update<AppLockModel, AppLockEvent, AppLockEffect> {

  override fun update(model: AppLockModel, event: AppLockEvent): Next<AppLockModel, AppLockEffect> {
    return when (event) {
      AppLockBackClicked -> dispatch(ExitApp)
      AppLockForgotPinClicked -> dispatch(ShowConfirmResetPinDialog)
      UnlockApp -> dispatch(RestorePreviousScreen)
      AppLockPinAuthenticated -> dispatch(LoadDataProtectionConsent)
      is LoggedInUserLoaded -> next(model.userLoaded(event.user))
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility))
      is DataProtectionConsentLoaded -> dataProtectionConsentLoaded(event.hasUserConsentedToDataProtection, model)
    }
  }

  private fun dataProtectionConsentLoaded(hasUserConsentedToDataProtection: Boolean, model: AppLockModel): Next<AppLockModel, AppLockEffect> {
    val effects = if (hasUserConsentedToDataProtection) {
      setOf(UnlockOnAuthentication)
    } else {
      emptySet()
    }

    return Next.next(model.dataProtectionConsentLoaded(hasUserConsentedToDataProtection), effects)
  }
}
