package org.simple.clinic.bp.entry.confirmremovebloodpressure

import java.util.UUID

sealed class ConfirmRemoveBloodPressureEffect

data object CloseDialog : ConfirmRemoveBloodPressureEffect()

data class DeleteBloodPressure(val bloodPressureMeasurementUuid: UUID) : ConfirmRemoveBloodPressureEffect()
