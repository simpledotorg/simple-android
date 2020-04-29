package org.simple.clinic.drugs

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class EditMedicinesInit : Init<EditMedicinesModel, EditMedicinesEffect> {

  override fun init(model: EditMedicinesModel): First<EditMedicinesModel, EditMedicinesEffect> {
    return first(model, FetchPrescribedAndProtocolDrugs(model.patientUuid))
  }
}
