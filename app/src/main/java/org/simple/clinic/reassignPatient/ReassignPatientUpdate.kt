package org.simple.clinic.reassignPatient

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class ReassignPatientUpdate : Update<ReassignPatientModel, ReassignPatientEvent, ReassignPatientEffect> {

  override fun update(
      model: ReassignPatientModel,
      event: ReassignPatientEvent
  ): Next<ReassignPatientModel, ReassignPatientEffect> {
    return when (event) {
      is AssignedFacilityLoaded -> Next.noChange()
    }
  }
}
