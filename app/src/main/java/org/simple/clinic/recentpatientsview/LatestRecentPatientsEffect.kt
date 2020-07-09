package org.simple.clinic.recentpatientsview

sealed class LatestRecentPatientsEffect

data class LoadRecentPatients(val count: Int): LatestRecentPatientsEffect()
