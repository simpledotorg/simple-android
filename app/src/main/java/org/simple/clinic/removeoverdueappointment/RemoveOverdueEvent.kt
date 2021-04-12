package org.simple.clinic.removeoverdueappointment

sealed class RemoveOverdueEvent

object PatientMarkedAsVisited : RemoveOverdueEvent()

object PatientMarkedAsDead : RemoveOverdueEvent()
