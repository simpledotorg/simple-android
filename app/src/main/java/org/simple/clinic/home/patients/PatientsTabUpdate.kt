package org.simple.clinic.home.patients

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL_SECURITY
import org.simple.clinic.appupdate.AppUpdateNudgePriority.LIGHT
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.Found
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.NotFound
import org.simple.clinic.drugstockreminders.DrugStockReminder.Result.OtherError
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.user.User
import java.time.Duration
import java.util.Optional

class PatientsTabUpdate(private val isNotifyAppUpdateAvailableV2Enabled: Boolean) : Update<PatientsTabModel, PatientsTabEvent, PatientsTabEffect> {

  override fun update(
      model: PatientsTabModel,
      event: PatientsTabEvent
  ): Next<PatientsTabModel, PatientsTabEffect> {
    return when (event) {
      is PatientsEnterCodeManuallyClicked -> dispatch(OpenEnterOtpScreen)
      NewPatientClicked -> dispatch(OpenPatientSearchScreen(null))
      is UserDetailsLoaded -> showAccountNotifications(model, event)
      is ActivityResumed -> dispatch(RefreshUserDetails)
      is DataForShowingApprovedStatusLoaded -> showUserApprovedStatus(event)
      is UserApprovedStatusDismissed -> dispatch(HideUserAccountStatus, SetDismissedApprovalStatus(dismissedStatus = true))
      is ScanCardIdButtonClicked -> openScanBpPassportScreen(event)
      is LoadedNumberOfPatientsRegistered -> next(model.numberOfPatientsRegisteredUpdated(event.numberOfPatientsRegistered))
      SimpleVideoClicked -> dispatch(OpenTrainingVideo)
      is RequiredInfoForShowingAppUpdateLoaded -> showAppUpdateAvailableMessageBasedOnFeatureFlag(model, event)
      is AppStalenessLoaded -> next(model.updateAppStaleness(event.appStaleness))
      UpdateNowButtonClicked -> dispatch(OpenSimpleOnPlayStore)
      is DrugStockReportLoaded -> drugStockReportLoaded(event, model)
      is RequiredInfoForShowingDrugStockReminderLoaded -> requiredInfoForDrugStockReminderLoaded(event, model)
      is EnterDrugStockButtonClicked -> enterDrugStockButtonClicked(event)
    }
  }

  private fun enterDrugStockButtonClicked(
      event: EnterDrugStockButtonClicked
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val effect = if (event.hasNetworkConnection) {
      OpenEnterDrugStockScreen
    } else {
      ShowNoActiveNetworkConnectionDialog
    }
    
    return dispatch(effect)
  }

  private fun drugStockReportLoaded(
      event: DrugStockReportLoaded,
      model: PatientsTabModel
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val isDrugStockReportFilled = when (event.result) {
      is Found -> true
      NotFound -> false
      OtherError -> throw IllegalArgumentException("Failed to get drug stock report")
    }

    return next(
        model.updateIsDrugStockFilled(Optional.of(isDrugStockReportFilled)),
        TouchDrugStockReportLastCheckedAt,
        TouchIsDrugStockReportFilled(isDrugStockReportFilled)
    )
  }

  private fun requiredInfoForDrugStockReminderLoaded(
      event: RequiredInfoForShowingDrugStockReminderLoaded,
      model: PatientsTabModel
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val currentDate = event.currentDate
    val drugStockReportLastCheckedAt = event.drugStockReportLastCheckedAt

    val hasADayPassedSinceDrugStockReportIsLastChecked = drugStockReportLastCheckedAt.isBefore(currentDate)
    val drugStockReportDate = currentDate.minusMonths(1).withDayOfMonth(1)

    return if (hasADayPassedSinceDrugStockReportIsLastChecked) {
      dispatch(LoadDrugStockReportStatus(drugStockReportDate.toString()))
    } else {
      next(model.updateIsDrugStockFilled(event.isDrugStockReportFilled))
    }
  }

  private fun showAccountNotifications(
      model: PatientsTabModel,
      event: UserDetailsLoaded
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val previousUser = model.user
    val newUser = event.user
    val updatedModel = model.userLoaded(newUser)

    val effects = mutableSetOf<PatientsTabEffect>()

    if (newUser.isWaitingForApproval) {
      // User is waiting for approval (new registration or login on a new device before being approved).
      clearDismissedApprovalStatusIfNeeded(previousUser, effects)
    }

    if (previousUser == null || previousUser.isWaitingForApproval) {
      checkIfUserIsApproved(newUser, effects)
    }
    return next(updatedModel, effects)
  }

  private fun checkIfUserIsApproved(newUser: User, effects: MutableSet<PatientsTabEffect>) {
    if (newUser.isApprovedForSyncing && !newUser.isPendingSmsVerification) {
      // User was just approved
      effects.add(LoadInfoForShowingApprovalStatus)
    }
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

  private fun showAppUpdateAvailableDialog_Old(event: RequiredInfoForShowingAppUpdateLoaded): Next<PatientsTabModel, PatientsTabEffect> {
    val appUpdateLastShownOn = event.appUpdateLastShownOn
    val currentDate = event.currentDate

    val hasADayPassedSinceUpdateLastShown = appUpdateLastShownOn.isBefore(currentDate)

    val shouldShowAppUpdate = event.isAppUpdateAvailable && hasADayPassedSinceUpdateLastShown

    return if (shouldShowAppUpdate)
      dispatch(ShowAppUpdateAvailable, TouchAppUpdateShownAtTime)
    else
      noChange()
  }

  private fun showAppUpdateAvailableMessageBasedOnFeatureFlag(
      model: PatientsTabModel,
      event: RequiredInfoForShowingAppUpdateLoaded
  ): Next<PatientsTabModel, PatientsTabEffect> {
    return if (isNotifyAppUpdateAvailableV2Enabled) {
      appUpdateNudgeBasedOnPriority(model, event)
    } else {
      showAppUpdateAvailableDialog_Old(event)
    }
  }

  private fun appUpdateNudgeBasedOnPriority(
      model: PatientsTabModel,
      event: RequiredInfoForShowingAppUpdateLoaded
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val updatedModel = model.appUpdateNudgePriorityUpdated(event.appUpdateNudgePriority).updateAppStaleness(event.appStaleness)

    return when (event.appUpdateNudgePriority) {
      LIGHT, MEDIUM -> showAppUpdateAvailableDialog(updatedModel, event)
      CRITICAL, CRITICAL_SECURITY -> next(updatedModel, ShowCriticalAppUpdateDialog(event.appUpdateNudgePriority))
      else -> noChange()
    }
  }

  private fun showAppUpdateAvailableDialog(
      model: PatientsTabModel,
      event: RequiredInfoForShowingAppUpdateLoaded
  ): Next<PatientsTabModel, PatientsTabEffect> {
    val appUpdateLastShownOn = event.appUpdateLastShownOn
    val currentDate = event.currentDate

    val hasADayPassedSinceUpdateLastShown = appUpdateLastShownOn.isBefore(currentDate)

    val shouldShowAppUpdate = event.isAppUpdateAvailable && hasADayPassedSinceUpdateLastShown

    return if (shouldShowAppUpdate)
      next(model, ShowAppUpdateAvailable, TouchAppUpdateShownAtTime)
    else
      noChange()
  }
}
