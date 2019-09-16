package org.simple.clinic.allpatientsinfacility.migration

import io.reactivex.Observable
import org.simple.clinic.widgets.UiEvent

interface ExposesUiEvents {
  val uiEvents: Observable<UiEvent>
}
