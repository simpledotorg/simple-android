package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import java.util.UUID

sealed class ConfirmRemoveBloodSugarEffect

data class MarkBloodSugarAsDeleted(val bloodSugarMeasurementUuid: UUID) : ConfirmRemoveBloodSugarEffect()

object CloseConfirmRemoveBloodSugarDialog : ConfirmRemoveBloodSugarEffect()
