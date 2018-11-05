package org.simple.clinic.editpatient

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PatientEditScreenCreated(val patientUuid: UUID): UiEvent
