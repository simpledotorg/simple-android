package org.simple.clinic.search

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.widgets.UiEvent

private typealias Ui = PatientSearchUi
private typealias UiChange = (Ui) -> Unit

class PatientSearchScreenController @AssistedInject constructor(
    @Assisted private val additionalIdentifier: Identifier?
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(additionalIdentifier: Identifier?): PatientSearchScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }

}
