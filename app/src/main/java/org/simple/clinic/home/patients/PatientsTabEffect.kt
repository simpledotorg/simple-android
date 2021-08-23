package org.simple.clinic.home.patients

import org.simple.clinic.patient.businessid.Identifier

sealed class PatientsTabEffect

object RefreshUserDetails : PatientsTabEffect()

object LoadUser : PatientsTabEffect()

object LoadInfoForShowingApprovalStatus : PatientsTabEffect()

data class SetDismissedApprovalStatus(val dismissedStatus: Boolean) : PatientsTabEffect()

object LoadNumberOfPatientsRegistered : PatientsTabEffect()

object OpenTrainingVideo : PatientsTabEffect()

object LoadInfoForShowingAppUpdateMessage : PatientsTabEffect()

object TouchAppUpdateShownAtTime : PatientsTabEffect()

object ShowAppUpdateAvailable : PatientsTabEffect()

sealed class PatientsTabViewEffect : PatientsTabEffect()

object OpenEnterOtpScreen : PatientsTabViewEffect()

data class OpenPatientSearchScreen(val additionalIdentifier: Identifier?) : PatientsTabViewEffect()

object ShowUserWasApproved : PatientsTabViewEffect()

object HideUserAccountStatus : PatientsTabViewEffect()

object OpenScanBpPassportScreen : PatientsTabViewEffect()
