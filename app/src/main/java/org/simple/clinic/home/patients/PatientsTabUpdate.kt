package org.simple.clinic.home.patients

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User
import java.time.Duration

class PatientsTabUpdate : Update<PatientsTabModel, PatientsTabEvent, PatientsTabEffect> {

  override fun update(model: PatientsTabModel, event: PatientsTabEvent): Next<PatientsTabModel, PatientsTabEffect> {
    return when (event) {
      is PatientsEnterCodeManuallyClicked -> dispatch(OpenEnterOtpScreen)
      NewPatientClicked -> dispatch(OpenPatientSearchScreen)
      is UserDetailsLoaded -> showAccountNotifications(model, event)
      is ActivityResumed -> dispatch(RefreshUserDetails)
      is DataForShowingApprovedStatusLoaded -> showUserApprovedStatus(event)
      is UserApprovedStatusDismissed -> dispatch(HideUserAccountStatus, SetDismissedApprovalStatus(dismissedStatus = true))
      is ScanCardIdButtonClicked -> openScanBpPassportScreen(event)
      is LoadedNumberOfPatientsRegistered -> next(model.numberOfPatientsRegisteredUpdated(event.numberOfPatientsRegistered))
      SimpleVideoClicked -> dispatch(OpenTrainingVideo)
      is RequiredInfoForShowingAppUpdateLoaded -> showAppUpdateAvailableMessage(event)
      is PatientSearchByIdentifierCompleted -> noChange()
      is BusinessIdScanned.ByIdentifier -> noChange()
      is BusinessIdScanned.ByShortCode -> dispatch(OpenShortCodeSearchScreen(event.shortCode))
    }
  }

  // TODO (vs) 26/05/20: This should actually be rendered and not be as effects. Move later.
  private fun showAccountNotifications(
      model: PatientsTabModel,
      event: UserDetailsLoaded
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val previousUser = model.user
    val newUser = event.user
    val updatedModel = model.userLoaded(newUser)

    val effects = mutableSetOf<PatientsTabEffect>()

    when {
      previousUser == null && newUser.isPendingSmsVerification -> {
        effects.add(ShowUserPendingSmsVerification)
      }

      newUser.isWaitingForApproval -> {
        // User is waiting for approval (new registration or login on a new device before being approved).
        effects.add(ShowUserAwaitingApproval)
        clearDismissedApprovalStatusIfNeeded(previousUser, effects)
      }

      newUser.isApprovedForSyncing && (previousUser == null || previousUser.isWaitingForApproval) -> {
        // User was just approved
        effects.add(LoadInfoForShowingApprovalStatus)
      }

      else -> effects.add(HideUserAccountStatus)
    }

    return next(updatedModel, effects)
  }

  private fun clearDismissedApprovalStatusIfNeeded(
      user: User?,
      effects: MutableSet<PatientsTabEffect>
  ) {
    if (user != null && user.isApprovedForSyncing) {
      // User was approved, but decided to proceed with the Reset PIN flow.
      effects.add(SetDismissedApprovalStatus(dismissedStatus = false))
    }
  }

  private fun showUserApprovedStatus(
      event: DataForShowingApprovedStatusLoaded
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val twentyFourHoursAgo = event.currentTime.minus(Duration.ofHours(24))
    val wasApprovedInLastTwentyFourHours = event.approvalStatusUpdatedAt.isAfter(twentyFourHoursAgo)

    return if (!event.hasBeenDismissed && wasApprovedInLastTwentyFourHours)
      dispatch(ShowUserWasApproved as PatientsTabEffect)
    else
      dispatch(HideUserAccountStatus)
  }

  private fun openScanBpPassportScreen(event: ScanCardIdButtonClicked): Next<PatientsTabModel, PatientsTabEffect> {
    return if (event.isPermissionGranted)
      dispatch(OpenScanBpPassportScreen)
    else
      noChange()
  }

  private fun showAppUpdateAvailableMessage(event: RequiredInfoForShowingAppUpdateLoaded): Next<PatientsTabModel, PatientsTabEffect> {
    val appUpdateLastShownOn = event.appUpdateLastShownOn
    val currentDate = event.currentDate

    val hasADayPassedSinceUpdateLastShown = appUpdateLastShownOn.isBefore(currentDate)

    val shouldShowAppUpdate = event.isAppUpdateAvailable && hasADayPassedSinceUpdateLastShown

    return if (shouldShowAppUpdate)
      dispatch(ShowAppUpdateAvailable, TouchAppUpdateShownAtTime)
    else
      noChange()
  }
}
