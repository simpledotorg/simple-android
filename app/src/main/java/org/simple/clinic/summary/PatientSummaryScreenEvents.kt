package org.simple.clinic.summary

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PatientSummaryScreenCreated(val patientUuid: UUID, val caller: PatientSummaryCaller) : UiEvent

class PatientSummaryBackClicked : UiEvent

class PatientSummaryNewBpClicked : UiEvent

class PatientSummaryUpdateMedicinesClicked : UiEvent
