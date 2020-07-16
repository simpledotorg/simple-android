package org.simple.clinic.home.overdue

import org.simple.clinic.facility.Facility
import java.time.LocalDate

sealed class OverdueEffect

object LoadCurrentFacility : OverdueEffect()

data class LoadOverdueAppointments(
    val overdueSince: LocalDate,
    val facility: Facility
) : OverdueEffect()
