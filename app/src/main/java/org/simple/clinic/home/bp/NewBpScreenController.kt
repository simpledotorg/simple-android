package org.simple.clinic.home.bp

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class NewBpScreenController @Inject constructor() : ObservableTransformer<UiEvent, (NewBpScreen) -> Unit> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<(NewBpScreen) -> Unit> {
    val function = { ui: NewBpScreen -> ui.openNewPatientScreen() }
    return events.ofType(NewPatientClicked::class.java)
        .map { function }
  }
}
