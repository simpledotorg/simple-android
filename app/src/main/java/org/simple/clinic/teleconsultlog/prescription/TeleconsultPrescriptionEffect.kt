package org.simple.clinic.teleconsultlog.prescription

import java.util.UUID

sealed class TeleconsultPrescriptionEffect

data class LoadPatientDetails(val patientUuid: UUID) : TeleconsultPrescriptionEffect()

data object GoBack : TeleconsultPrescriptionEffect()

data object ShowSignatureRequiredError : TeleconsultPrescriptionEffect()

data class LoadDataForNextClick(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID,
    val medicalInstructions: String,
    val medicalRegistrationId: String
) : TeleconsultPrescriptionEffect()

data class AddTeleconsultIdToPrescribedDrugs(
    val patientUuid: UUID,
    val teleconsultRecordId: UUID,
    val medicalInstructions: String
) : TeleconsultPrescriptionEffect()

data class OpenSharePrescriptionScreen(
    val patientUuid: UUID,
    val medicalInstructions: String
) : TeleconsultPrescriptionEffect()

data class SaveMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultPrescriptionEffect()

data class UpdateTeleconsultRecordMedicalRegistrationId(
    val teleconsultRecordId: UUID,
    val medicalRegistrationId: String
) : TeleconsultPrescriptionEffect()

data object ShowMedicinesRequiredError : TeleconsultPrescriptionEffect()
