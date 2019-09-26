package org.simple.clinic.editpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class EditPatientUpdate : Update<EditPatientModel, EditPatientEvent, EditPatientEffect> {
  override fun update(
      model: EditPatientModel,
      event: EditPatientEvent
  ): Next<EditPatientModel, EditPatientEffect> {
    TODO("not implemented")
  }
}
