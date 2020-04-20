package org.simple.clinic.drugs

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class EditMedicinesEffectHandler : ObservableTransformer<EditMedicinesEffect, EditMedicinesEvent> {

  override fun apply(upstream: Observable<EditMedicinesEffect>): ObservableSource<EditMedicinesEvent> {
    TODO("not implemented")
  }
}
