package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

sealed class ScanResult : Parcelable

@Parcelize
data class SearchByEnteredCode(val enteredCode: String) : ScanResult()

@Parcelize
data class PatientFound(val patientId: UUID) : ScanResult()

@Parcelize
data class PatientNotFound(val identifier: Identifier) : ScanResult()
