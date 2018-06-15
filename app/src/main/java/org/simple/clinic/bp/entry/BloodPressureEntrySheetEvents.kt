package org.simple.clinic.bp.entry

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class BloodPressureEntrySheetCreated(val patientUuid: UUID) : UiEvent

class BloodPressureSystolicTextChanged(val systolic: String) : UiEvent

class BloodPressureDiastolicTextChanged(val diastolic: String) : UiEvent

class BloodPressureSaveClicked : UiEvent
