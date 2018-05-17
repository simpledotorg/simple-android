package org.resolvetosavelives.red.newentry.mobile

import org.resolvetosavelives.red.widgets.UiEvent

class PatientMobileEntryProceedClicked : UiEvent

data class PatientPrimaryMobileTextChanged(val number: String) : UiEvent

data class PatientSecondaryMobileTextChanged(val number: String) : UiEvent
