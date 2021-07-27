package org.simple.clinic.contactpatient

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId

@Parcelize
data class ContactPatientProfile(
    @Embedded
    val patient: Patient,

    @Relation(
        parentColumn = "addressUuid",
        entityColumn = "uuid"
    )
    val address: PatientAddress,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val phoneNumbers: List<PatientPhoneNumber>,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val businessIds: List<BusinessId>,

    @Relation(
        parentColumn = "registeredFacilityId",
        entityColumn = "uuid"
    )
    val registeredFacility: Facility,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val medicalHistory: MedicalHistory?,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val bloodSugarMeasurement: BloodSugarMeasurement?,

    @Relation(
        parentColumn = "uuid",
        entityColumn = "patientUuid"
    )
    val bloodPressureMeasurement: BloodPressureMeasurement?
) : Parcelable
