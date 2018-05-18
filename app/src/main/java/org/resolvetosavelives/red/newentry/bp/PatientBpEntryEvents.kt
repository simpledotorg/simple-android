package org.resolvetosavelives.red.newentry.bp

import org.resolvetosavelives.red.widgets.UiEvent

data class PatientBpSystolicTextChanged(val measurement: Int) : UiEvent

data class PatientBpDiastolicTextChanged(val measurement: Int) : UiEvent

class PatientBpEntryProceedClicked : UiEvent
