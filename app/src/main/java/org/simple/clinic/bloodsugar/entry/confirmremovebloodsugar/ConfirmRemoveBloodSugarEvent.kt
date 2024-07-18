package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import org.simple.clinic.widgets.UiEvent

sealed class ConfirmRemoveBloodSugarEvent : UiEvent

data object BloodSugarMarkedAsDeleted : ConfirmRemoveBloodSugarEvent()

data object RemoveBloodSugarClicked : ConfirmRemoveBloodSugarEvent()
