package org.simple.clinic.recentpatientsview

import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.widgets.UiEvent

sealed class LatestRecentPatientsEvent : UiEvent

data class RecentPatientsLoaded(val recentPatients: List<RecentPatient>): LatestRecentPatientsEvent()
