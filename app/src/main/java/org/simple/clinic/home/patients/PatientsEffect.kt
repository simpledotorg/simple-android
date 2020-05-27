package org.simple.clinic.home.patients

sealed class PatientsEffect

object OpenEnterOtpScreen : PatientsEffect()

object OpenPatientSearchScreen : PatientsEffect()

object RefreshUserDetails : PatientsEffect()

object LoadUser : PatientsEffect()

object LoadInfoForShowingApprovalStatus : PatientsEffect()

object ShowUserAwaitingApproval : PatientsEffect()

data class SetDismissedApprovalStatus(val dismissedStatus: Boolean) : PatientsEffect()

object ShowUserWasApproved: PatientsEffect()

object ShowUserPendingSmsVerification: PatientsEffect()

object HideUserAccountStatus: PatientsEffect()

object OpenScanBpPassportScreen: PatientsEffect()

object LoadNumberOfPatientsRegistered: PatientsEffect()

object OpenTrainingVideo: PatientsEffect()

object LoadInfoForShowingAppUpdateMessage: PatientsEffect()
