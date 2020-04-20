package org.simple.clinic.drugs

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class EditMedicinesUpdate : Update<EditMedicinesModel, EditMedicinesEvent, EditMedicinesEffect> {

  override fun update(model: EditMedicinesModel, event: EditMedicinesEvent): Next<EditMedicinesModel, EditMedicinesEffect> {
    return Next.next(model)
  }
}
