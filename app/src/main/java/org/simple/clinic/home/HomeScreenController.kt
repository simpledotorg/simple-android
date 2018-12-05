package org.simple.clinic.home

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = HomeScreen
typealias UiChange = (Ui) -> Unit

class HomeScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return currentFacility(replayedEvents)
  }

  private fun currentFacility(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<ScreenCreated>()
        .flatMap { facilityRepository.currentFacility(userSession) }
        .map { { ui: Ui -> ui.setFacility(it.name) } }
  }

}
