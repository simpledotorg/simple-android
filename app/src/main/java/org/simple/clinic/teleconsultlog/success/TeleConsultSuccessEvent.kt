package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.patient.Patient

sealed class TeleConsultSuccessEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleConsultSuccessEvent()

object NoPrescriptionClicked : TeleConsultSuccessEvent()

object YesPrescriptionClicked : TeleConsultSuccessEvent()
