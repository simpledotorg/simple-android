package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class AllPatientsInFacilityUiController @Inject constructor(
    private val uiStateProducer: AllPatientsInFacilityUiStateProducer,
    private val uiChangeProducer: AllPatientsInFacilityUiChangeProducer
) : ObservableTransformer<UiEvent, AllPatientsInFacilityUiChange> {

  override fun apply(uiEvents: Observable<UiEvent>): ObservableSource<AllPatientsInFacilityUiChange> {
    return uiEvents
        .compose(uiStateProducer)
        .share()
        .compose(uiChangeProducer)
  }
}
