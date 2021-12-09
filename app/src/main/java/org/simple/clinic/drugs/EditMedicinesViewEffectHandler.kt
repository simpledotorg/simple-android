package org.simple.clinic.drugs

import org.simple.clinic.drugs.selection.EditMedicinesUiActions
import org.simple.clinic.mobius.ViewEffectsHandler

class EditMedicinesViewEffectHandler(
    private val uiActions: EditMedicinesUiActions
) : ViewEffectsHandler<EditMedicinesViewEffect> {
  override fun handle(viewEffect: EditMedicinesViewEffect) {
    // does nothing, yet.
  }
}
