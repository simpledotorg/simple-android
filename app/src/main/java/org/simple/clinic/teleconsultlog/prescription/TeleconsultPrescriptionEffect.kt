package org.simple.clinic.teleconsultlog.prescription

import java.util.UUID

sealed class TeleconsultPrescriptionEffect

data class LoadPatientDetails(val patientUuid: UUID) : TeleconsultPrescriptionEffect()

object GoBack : TeleconsultPrescriptionEffect()

object ShowSignatureRequiredError : TeleconsultPrescriptionEffect()

data class LoadDataForNextClick(
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
    val teleconsultRecordId: UUID,
    val medicalInstructions: String
) : TeleconsultPrescriptionEffect()

data class SaveMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultPrescriptionEffect()

data class UpdateTeleconsultRecordMedicalRegistrationId(val teleconsultRecordId: UUID, val medicalRegistrationId: String) : TeleconsultPrescriptionEffect()

object ShowMedicinesRequiredError : TeleconsultPrescriptionEffect()
