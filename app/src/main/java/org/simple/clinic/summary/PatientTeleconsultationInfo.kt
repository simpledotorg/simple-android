package org.simple.clinic.summary

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import java.util.UUID

data class PatientTeleconsultationInfo(
    val patientUuid: UUID,
    val bpPassport: String?,
    val facility: Facility,
    val bloodPressures: List<BloodPressureMeasurement>,
    val prescriptions: List<PrescribedDrug>
)
