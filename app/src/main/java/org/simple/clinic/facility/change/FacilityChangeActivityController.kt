package org.simple.clinic.facility.change

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = FacilityChangeUi
typealias UiChange = (Ui) -> Unit

class FacilityChangeActivityController @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
