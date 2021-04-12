package org.simple.clinic.removeoverdueappointment

sealed class RemoveOverdueEvent

object PatientMarkedAsVisited : RemoveOverdueEvent()
