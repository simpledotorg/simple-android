package org.resolvetosavelives.red.summary

import org.resolvetosavelives.red.widgets.UiEvent
import java.util.UUID

data class PatientSummaryScreenCreated(val patientUuid: UUID, val caller: PatientSummaryCaller) : UiEvent

class PatientSummaryBackClicked() : UiEvent
