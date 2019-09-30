package org.simple.clinic.editpatient_old

import org.simple.clinic.editpatient.EditablePatientEntry
import org.simple.clinic.widgets.UiEvent

data class OngoingEditPatientEntryChanged(val ongoingEntry: EditablePatientEntry): UiEvent
