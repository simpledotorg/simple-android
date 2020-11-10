package org.simple.clinic.home.patients

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class PatientsTabEffect

object OpenEnterOtpScreen : PatientsTabEffect()

object OpenPatientSearchScreen : PatientsTabEffect()

object RefreshUserDetails : PatientsTabEffect()

object LoadUser : PatientsTabEffect()

object LoadInfoForShowingApprovalStatus : PatientsTabEffect()

object ShowUserAwaitingApproval : PatientsTabEffect()

data class SetDismissedApprovalStatus(val dismissedStatus: Boolean) : PatientsTabEffect()

object ShowUserWasApproved: PatientsTabEffect()

object ShowUserPendingSmsVerification: PatientsTabEffect()

object HideUserAccountStatus: PatientsTabEffect()

object OpenScanBpPassportScreen: PatientsTabEffect()

object LoadNumberOfPatientsRegistered: PatientsTabEffect()

object OpenTrainingVideo: PatientsTabEffect()

object LoadInfoForShowingAppUpdateMessage: PatientsTabEffect()

object TouchAppUpdateShownAtTime: PatientsTabEffect()

object ShowAppUpdateAvailable: PatientsTabEffect()

data class OpenShortCodeSearchScreen(val shortCode: String): PatientsTabEffect()

data class SearchPatientByIdentifier(val identifier: Identifier): PatientsTabEffect()

data class OpenPatientSummary(val patientId: UUID): PatientsTabEffect()
