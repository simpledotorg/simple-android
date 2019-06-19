package org.simple.clinic.allpatientsinfacility

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.widgets.UiEvent

class AllPatientsInFacilityViewController(
    private val viewStateProducer: AllPatientsInFacilityViewStateProducer,
    private val uiChangeProducer: AllPatientsInFacilityUiChangeProducer
) : ObservableTransformer<UiEvent, AllPatientsInFacilityUiChange> {

  override fun apply(uiEvents: Observable<UiEvent>): ObservableSource<AllPatientsInFacilityUiChange> {
    return uiEvents
        .compose(viewStateProducer)
        .compose(uiChangeProducer)
  }
}
