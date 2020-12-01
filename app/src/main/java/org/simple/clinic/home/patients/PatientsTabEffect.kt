package org.simple.clinic.home.patients

import org.simple.clinic.patient.businessid.Identifier

sealed class PatientsTabEffect

object OpenEnterOtpScreen : PatientsTabEffect()

data class OpenPatientSearchScreen(val additionalIdentifier: Identifier?) : PatientsTabEffect()

object RefreshUserDetails : PatientsTabEffect()

object LoadUser : PatientsTabEffect()

object LoadInfoForShowingApprovalStatus : PatientsTabEffect()

object ShowUserAwaitingApproval : PatientsTabEffect()

data class SetDismissedApprovalStatus(val dismissedStatus: Boolean) : PatientsTabEffect()

object ShowUserWasApproved : PatientsTabEffect()

object ShowUserPendingSmsVerification : PatientsTabEffect()

object HideUserAccountStatus : PatientsTabEffect()

object OpenScanBpPassportScreen : PatientsTabEffect()

object LoadNumberOfPatientsRegistered : PatientsTabEffect()

object OpenTrainingVideo : PatientsTabEffect()

object LoadInfoForShowingAppUpdateMessage : PatientsTabEffect()

object TouchAppUpdateShownAtTime : PatientsTabEffect()

object ShowAppUpdateAvailable : PatientsTabEffect()
