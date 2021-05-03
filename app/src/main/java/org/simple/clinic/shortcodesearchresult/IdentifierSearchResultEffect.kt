package org.simple.clinic.shortcodesearchresult

import java.util.UUID

sealed class IdentifierSearchResultEffect

data class OpenPatientSummary(val patientId: UUID): IdentifierSearchResultEffect()

object OpenPatientSearch: IdentifierSearchResultEffect()

data class SearchByShortCode(val shortCode: String): IdentifierSearchResultEffect()
