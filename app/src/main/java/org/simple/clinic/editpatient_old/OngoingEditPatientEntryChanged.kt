package org.simple.clinic.editpatient_old

import org.simple.clinic.editpatient.OngoingEditPatientEntry
import org.simple.clinic.widgets.UiEvent

data class OngoingEditPatientEntryChanged(val ongoingEditPatientEntry: OngoingEditPatientEntry): UiEvent
