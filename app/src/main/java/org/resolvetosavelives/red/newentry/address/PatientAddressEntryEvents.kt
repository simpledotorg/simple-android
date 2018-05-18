package org.resolvetosavelives.red.newentry.address

import org.resolvetosavelives.red.widgets.UiEvent

class PatientAddressEntryProceedClicked : UiEvent

data class PatientAddressColonyOrVillageTextChanged(val colonyOrVillage: String) : UiEvent

data class PatientAddressDistrictTextChanged(val district: String) : UiEvent

data class PatientAddressStateTextChanged(val state: String) : UiEvent
