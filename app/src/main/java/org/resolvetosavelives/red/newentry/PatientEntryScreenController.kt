package org.resolvetosavelives.red.newentry

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent
import javax.inject.Inject

typealias UiChange = (PatientEntryScreen) -> Unit

class PatientEntryScreenController @Inject constructor(
    val repository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()
    return Observable.mergeArray(preFillOnStart(replayedEvents))
  }

  private fun preFillOnStart(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle { repository.ongoingEntry() }
        .map { { ui: PatientEntryScreen -> ui.preFillFields(it) } }
  }
}
