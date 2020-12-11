package org.simple.clinic.shortcodesearchresult

import java.util.UUID

sealed class ShortCodeSearchResultEffect

data class OpenPatientSummary(val patientId: UUID): ShortCodeSearchResultEffect()
