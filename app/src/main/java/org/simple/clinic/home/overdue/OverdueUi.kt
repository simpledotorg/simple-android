package org.simple.clinic.home.overdue

interface OverdueUi {
  fun showOverdueAppointments(
      pendingAppointments: List<OverdueAppointment>,
      agreedToVisitAppointments: List<OverdueAppointment>,
      remindToCallLaterAppointments: List<OverdueAppointment>,
      removedFromOverdueAppointments: List<OverdueAppointment>,
      moreThanAnYearOverdueAppointments: List<OverdueAppointment>
  )
}
