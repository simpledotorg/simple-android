package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.subjects.Subject
import org.simple.clinic.widgets.UiEvent

class AllPatientsInFacilityUiController(
    private val uiStateProducer: AllPatientsInFacilityUiStateProducer,
    private val uiChangeProducer: AllPatientsInFacilityUiChangeProducer,
    private val states: Subject<AllPatientsInFacilityUiState>
) : ObservableTransformer<UiEvent, AllPatientsInFacilityUiChange> {

  override fun apply(uiEvents: Observable<UiEvent>): ObservableSource<AllPatientsInFacilityUiChange> {
    return uiEvents
        .compose(uiStateProducer)
        .doOnNext { states.onNext(it) }
        .compose(uiChangeProducer)
  }
}
