package org.simple.clinic.bloodsugar.history.adapter

import org.simple.clinic.bloodsugar.BloodSugarMeasurement

sealed class Event

data object NewBloodSugarClicked : Event()

data class BloodSugarHistoryItemClicked(val measurement: BloodSugarMeasurement) : Event()
