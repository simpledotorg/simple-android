package org.simple.clinic.home.patients

import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.patient.businessid.Identifier

sealed class PatientsTabEffect

data object RefreshUserDetails : PatientsTabEffect()

data object LoadUser : PatientsTabEffect()

data object LoadInfoForShowingApprovalStatus : PatientsTabEffect()

data class SetDismissedApprovalStatus(val dismissedStatus: Boolean) : PatientsTabEffect()

data object LoadInfoForShowingAppUpdateMessage : PatientsTabEffect()

data object TouchAppUpdateShownAtTime : PatientsTabEffect()

data object LoadAppStaleness : PatientsTabEffect()

data object ScheduleAppUpdateNotification : PatientsTabEffect()

data class LoadDrugStockReportStatus(val date: String) : PatientsTabEffect()

data object LoadInfoForShowingDrugStockReminder : PatientsTabEffect()

data object TouchDrugStockReportLastCheckedAt : PatientsTabEffect()

data class TouchIsDrugStockReportFilled(val isDrugStockReportFilled: Boolean) : PatientsTabEffect()

sealed class PatientsTabViewEffect : PatientsTabEffect()

data object OpenEnterOtpScreen : PatientsTabViewEffect()

data class OpenPatientSearchScreen(val additionalIdentifier: Identifier?) : PatientsTabViewEffect()

data object ShowUserWasApproved : PatientsTabViewEffect()

data object HideUserAccountStatus : PatientsTabViewEffect()

data object OpenScanBpPassportScreen : PatientsTabViewEffect()

data object ShowAppUpdateAvailable : PatientsTabViewEffect()

data object OpenSimpleOnPlayStore : PatientsTabViewEffect()

data class ShowCriticalAppUpdateDialog(val appUpdateNudgePriority: AppUpdateNudgePriority) : PatientsTabViewEffect()

data object OpenEnterDrugStockScreen : PatientsTabViewEffect()

data object ShowNoActiveNetworkConnectionDialog : PatientsTabViewEffect()

data object OpenDrugStockReportsForm : PatientsTabViewEffect()
