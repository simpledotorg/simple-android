package org.simple.clinic.home.patients

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class PatientsUpdate : Update<PatientsModel, PatientsEvent, PatientsEffect> {

  override fun update(model: PatientsModel, event: PatientsEvent): Next<PatientsModel, PatientsEffect> {
    return when (event) {
      is PatientsEnterCodeManuallyClicked -> dispatch(OpenEnterOtpScreen)
      NewPatientClicked -> dispatch(OpenPatientSearchScreen)
      is UserDetailsLoaded -> showAccountNotifications(model, event)
      is ActivityResumed -> dispatch(RefreshUserDetails)
      is DataForShowingApprovedStatusLoaded -> noChange()
    }
  }

  private fun showAccountNotifications(
      model: PatientsModel,
      event: UserDetailsLoaded
  ): Next<PatientsModel, PatientsEffect> {
    val previousUser = model.user
    val newUser = event.user
    val updatedModel = model.userLoaded(newUser)

    val effects = mutableSetOf<PatientsEffect>()

    when {
      previousUser == null && newUser.isWaitingForApproval -> {
        // User is waiting for approval (new registration or login on a new device before being approved).
        effects.add(ShowUserAwaitingApproval)
      }

      previousUser != null && previousUser.isApprovedForSyncing && newUser.isWaitingForApproval -> {
        // User was approved, but decided to proceed with the Reset PIN flow.
        effects.add(ShowUserAwaitingApproval)
        effects.add(SetDismissedApprovalStatus(false))
      }
    }

    return next(updatedModel, effects)
  }
}
