package org.simple.clinic.removeoverdueappointment

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class RemoveOverdueUpdate : Update<RemoveOverdueModel, RemoveOverdueEvent, RemoveOverdueEffect> {

  override fun update(model: RemoveOverdueModel, event: RemoveOverdueEvent): Next<RemoveOverdueModel, RemoveOverdueEffect> {
    return when (event) {
      is RemoveAppointmentReasonSelected -> next(model.removeAppointmentReasonSelected(selectedReason = event.reason))
      is PatientMarkedAsMigrated -> dispatch(CancelAppointment(model.appointmentId, event.cancelReason))
      else -> noChange()
    }
  }
}
