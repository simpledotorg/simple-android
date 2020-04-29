package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import org.simple.clinic.widgets.UiEvent

sealed class ConfirmRemoveBloodSugarEvent : UiEvent

object BloodSugarMarkedAsDeleted : ConfirmRemoveBloodSugarEvent()

object RemoveBloodSugarClicked : ConfirmRemoveBloodSugarEvent()
