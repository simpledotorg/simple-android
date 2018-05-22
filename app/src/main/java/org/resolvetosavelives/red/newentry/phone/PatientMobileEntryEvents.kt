package org.resolvetosavelives.red.newentry.phone

import org.resolvetosavelives.red.widgets.UiEvent

class PatientPhoneEntryProceedClicked : UiEvent

data class PatientPrimaryPhoneTextChanged(val number: String) : UiEvent

data class PatientSecondaryPhoneTextChanged(val number: String) : UiEvent
