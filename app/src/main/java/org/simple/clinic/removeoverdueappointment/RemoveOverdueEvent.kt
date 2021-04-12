package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.overdue.AppointmentCancelReason

sealed class RemoveOverdueEvent

object PatientMarkedAsVisited : RemoveOverdueEvent()

object PatientMarkedAsDead : RemoveOverdueEvent()

object AppointmentMarkedAsCancelled : RemoveOverdueEvent()

data class PatientMarkedAsMigrated(val cancelReason: AppointmentCancelReason) : RemoveOverdueEvent()
