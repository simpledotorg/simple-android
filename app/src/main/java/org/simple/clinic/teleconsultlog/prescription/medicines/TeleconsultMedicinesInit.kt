package org.simple.clinic.teleconsultlog.prescription.medicines

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class TeleconsultMedicinesInit : Init<TeleconsultMedicinesModel, TeleconsultMedicinesEffect> {

  override fun init(model: TeleconsultMedicinesModel): First<TeleconsultMedicinesModel, TeleconsultMedicinesEffect> {
    return first(model, LoadPatientMedicines(model.patientUuid))
  }
}
