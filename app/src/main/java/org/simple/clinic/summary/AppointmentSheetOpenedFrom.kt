package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet

/**
 * When the [ScheduleAppointmentSheet] is closed, we need to go to a different screen from the
 * [PatientSummaryScreen] depending on whether the sheet was opened by clicking "Back" or "Save"
 * from the summary screen.
 *
 * In the v1 architecture, since we were replaying the events that happened since the screen was
 * opened, we could reach into the string using `.combineLatest()` or `.withLatestFrom()` and find
 * out what triggered the sheet to be shown.
 *
 * However with Mobius, we cannot do this anymore. We need a way to find out what was the event
 * that triggered the sheet to be opened and this enum will be used to track that.
 **/
@Parcelize
enum class AppointmentSheetOpenedFrom : Parcelable {
  BACK_CLICK,
  DONE_CLICK
}
