package org.resolvetosavelives.red.bp.entry

import org.resolvetosavelives.red.widgets.UiEvent
import java.util.UUID

data class BloodPressureEntrySheetCreated(val patientUuid: UUID) : UiEvent

class BloodPressureSystolicTextChanged(val systolic: String) : UiEvent

class BloodPressureDiastolicTextChanged(val diastolic: String) : UiEvent

class BloodPressureSaveClicked : UiEvent
