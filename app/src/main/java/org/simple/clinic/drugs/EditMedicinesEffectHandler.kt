package org.simple.clinic.drugs

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.selection.EditMedicinesUiActions

class EditMedicinesEffectHandler(val uiActions: EditMedicinesUiActions) : ObservableTransformer<EditMedicinesEffect, EditMedicinesEvent> {

  override fun apply(upstream: Observable<EditMedicinesEffect>): ObservableSource<EditMedicinesEvent> {
    return Observable.empty()
  }
}
