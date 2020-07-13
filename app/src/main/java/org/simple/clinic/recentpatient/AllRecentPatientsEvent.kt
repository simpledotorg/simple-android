package org.simple.clinic.recentpatient

import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.widgets.UiEvent

sealed class AllRecentPatientsEvent : UiEvent

data class RecentPatientsLoaded(val recentPatients: List<RecentPatient>) : AllRecentPatientsEvent()
