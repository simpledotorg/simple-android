package org.simple.clinic.patient.onlinelookup.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.ReminderConsent
import org.simple.clinic.patient.sync.BusinessIdPayload
import org.simple.clinic.patient.sync.PatientAddressPayload
import org.simple.clinic.patient.sync.PatientPhoneNumberPayload
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@JsonClass(generateAdapter = true)
data class PatientOnlineLookupResponsePayload(
    @Json(name = "id")
    val id: UUID,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "gender")
    val gender: Gender,

    @Json(name = "date_of_birth")
    val dateOfBirth: LocalDate?,

    @Json(name = "age")
    val age: Int?,

    @Json(name = "age_updated_at")
    val ageUpdatedAt: Instant?,

    @Json(name = "status")
    val status: PatientStatus,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "deleted_at")
    val deletedAt: Instant?,

    @Json(name = "address")
    val address: PatientAddressPayload,

    @Json(name = "phone_numbers")
    val phoneNumbers: List<PatientPhoneNumberPayload>?,

    @Json(name = "business_identifiers")
    val businessIds: List<BusinessIdPayload>,

    @Json(name = "recorded_at")
    val recordedAt: Instant,

    @Json(name = "reminder_consent")
    val reminderConsent: ReminderConsent,

    @Json(name = "deleted_reason")
    val deletedReason: DeletedReason?,

    @Json(name = "registration_facility_id")
    val registeredFacilityId: UUID?,

    @Json(name = "assigned_facility_id")
    val assignedFacilityId: UUID?,

    @Json(name = "appointments")
    val appointments: List<Appointment>,

    @Json(name = "blood_pressures")
    val bloodPressures: List<BloodPressureMeasurement>,

    @Json(name = "blood_sugars")
    val bloodSugars: List<BloodSugarMeasurement>,

    @Json(name = "medical_history")
    val medicalHistory: MedicalHistory,

    @Json(name = "prescribed_drugs")
    val prescribedDrugs: List<PrescribedDrug>,

    @Json(name = "retention")
    val retention: RecordRetention
)
