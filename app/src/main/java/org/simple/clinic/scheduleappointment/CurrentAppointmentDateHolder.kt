package org.simple.clinic.scheduleappointment

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.overdue.PotentialAppointmentDate

// Temporary class introduced to ease Mobius migration since both the
// controller and the Mobius loop and modify this date from different
// flows. This will be used to share a mutable reference to the current
// date and then once the migration is complete, we will remove this
// and store the date in the model directly
@Parcelize
data class CurrentAppointmentDateHolder(var date: PotentialAppointmentDate): Parcelable
