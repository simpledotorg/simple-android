package org.simple.clinic.summary

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistory
import java.util.UUID

sealed class PatientTeleconsultationInfo

data class PatientTeleconsultationInfoLong_Old(
    val patientUuid: UUID,
    val bpPassport: String?,
    val facility: Facility,
    val bloodPressures: List<BloodPressureMeasurement>,
    val bloodSugars: List<BloodSugarMeasurement>,
    val prescriptions: List<PrescribedDrug>,
    val medicalHistory: MedicalHistory
) : PatientTeleconsultationInfo()

data class PatientTeleconsultationInfoLong(
    val patientUuid: UUID,
    val teleconsultationId: UUID,
    val bpPassport: String?,
    val facility: Facility,
    val bloodPressures: List<BloodPressureMeasurement>,
    val bloodSugars: List<BloodSugarMeasurement>,
    val prescriptions: List<PrescribedDrug>,
    val medicalHistory: MedicalHistory,
    val nursePhoneNumber: String?,
    val doctorPhoneNumber: String
) : PatientTeleconsultationInfo()

data class PatientTeleconsultationInfoShort(
    val patientUuid: UUID,
    val teleconsultationId: UUID
) : PatientTeleconsultationInfo()
