package org.simple.clinic.home.patients

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class PatientsUpdate : Update<PatientsModel, PatientsEvent, PatientsEffect> {

  override fun update(model: PatientsModel, event: PatientsEvent): Next<PatientsModel, PatientsEffect> {
    return when(event) {
      is PatientsEnterCodeManuallyClicked -> dispatch(OpenEnterOtpScreen)
      NewPatientClicked -> dispatch(OpenPatientSearchScreen)
      is UserDetailsLoaded -> next(model.userLoaded(event.user))
      is ActivityResumed -> dispatch(RefreshUserDetails)
      is DismissedApprovalStatusLoaded -> noChange()
    }
  }
}
