package org.simple.clinic.shortcodesearchresult

import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class ShortCodeSearchResultEvent : UiEvent

data class ViewPatient(val patientUuid: UUID) : ShortCodeSearchResultEvent()

object SearchPatient : ShortCodeSearchResultEvent()

data class ShortCodeSearchCompleted(val results: PatientSearchResults): ShortCodeSearchResultEvent()
