package org.simple.clinic.home.patients

import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.patient.businessid.Identifier

sealed class PatientsTabEffect

object RefreshUserDetails : PatientsTabEffect()

object LoadUser : PatientsTabEffect()

object LoadInfoForShowingApprovalStatus : PatientsTabEffect()

data class SetDismissedApprovalStatus(val dismissedStatus: Boolean) : PatientsTabEffect()

object LoadInfoForShowingAppUpdateMessage : PatientsTabEffect()

object TouchAppUpdateShownAtTime : PatientsTabEffect()

object LoadAppStaleness : PatientsTabEffect()

object ScheduleAppUpdateNotification : PatientsTabEffect()

data class LoadDrugStockReportStatus(val date: String) : PatientsTabEffect()

object LoadInfoForShowingDrugStockReminder : PatientsTabEffect()

object TouchDrugStockReportLastCheckedAt : PatientsTabEffect()

data class TouchIsDrugStockReportFilled(val isDrugStockReportFilled: Boolean) : PatientsTabEffect()

sealed class PatientsTabViewEffect : PatientsTabEffect()

object OpenEnterOtpScreen : PatientsTabViewEffect()

data class OpenPatientSearchScreen(val additionalIdentifier: Identifier?) : PatientsTabViewEffect()

object ShowUserWasApproved : PatientsTabViewEffect()

object HideUserAccountStatus : PatientsTabViewEffect()

object OpenScanBpPassportScreen : PatientsTabViewEffect()

object ShowAppUpdateAvailable : PatientsTabViewEffect()

object OpenSimpleOnPlayStore : PatientsTabViewEffect()

data class ShowCriticalAppUpdateDialog(val appUpdateNudgePriority: AppUpdateNudgePriority) : PatientsTabViewEffect()

object OpenEnterDrugStockScreen : PatientsTabViewEffect()

object ShowNoActiveNetworkConnectionDialog : PatientsTabViewEffect()

object OpenDrugStockReportsForm : PatientsTabViewEffect()
