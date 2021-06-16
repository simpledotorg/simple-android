package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class TeleConsultSuccessEvent : UiEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleConsultSuccessEvent()

object NoPrescriptionClicked : TeleConsultSuccessEvent()

object YesPrescriptionClicked : TeleConsultSuccessEvent()
