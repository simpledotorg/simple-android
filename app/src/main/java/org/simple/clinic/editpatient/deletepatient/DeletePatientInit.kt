package org.simple.clinic.editpatient.deletepatient

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class DeletePatientInit : Init<DeletePatientModel, DeletePatientEffect> {
  override fun init(model: DeletePatientModel): First<DeletePatientModel, DeletePatientEffect> {
    return if (model.hasPatientName) {
      first(model)
    } else {
      first(model, LoadPatient(model.patientUuid))
    }
  }
}
