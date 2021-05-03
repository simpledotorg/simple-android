package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class IdentifierSearchResultEvent : UiEvent

data class ViewPatient(val patientUuid: UUID) : IdentifierSearchResultEvent()

object SearchPatient : IdentifierSearchResultEvent()

data class ShortCodeSearchCompleted(val results: PatientSearchResults): IdentifierSearchResultEvent()
