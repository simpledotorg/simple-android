package org.simple.clinic.scheduleappointment.facilityselection

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilitySelectionUi
typealias UiChange = (Ui) -> Unit

class FacilitySelectionActivityController @Inject constructor() : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }

}
