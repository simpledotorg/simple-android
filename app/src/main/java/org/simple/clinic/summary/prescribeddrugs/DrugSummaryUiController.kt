package org.simple.clinic.summary.prescribeddrugs

import com.f2prateek.rx.preferences2.Preference
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = DrugSummaryUi

typealias UiChange = (Ui) -> Unit

class DrugSummaryUiController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    private val repository: PrescriptionRepository,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID): DrugSummaryUiController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        populatePrescribedDrugs(replayedEvents),
        openPrescribedDrugsScreen(replayedEvents)
    )
  }

  private fun populatePrescribedDrugs(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .switchMap { repository.newestPrescriptionsForPatient(patientUuid) }
        .map { { ui: Ui -> ui.populatePrescribedDrugs(it) } }
  }

  private fun openPrescribedDrugsScreen(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<PatientSummaryUpdateDrugsClicked>()
        .map {
          val user = userSession.loggedInUserImmediate()!!
          val facility = facilityRepository.currentFacilityImmediate(user)!!
          { ui: Ui -> ui.showUpdatePrescribedDrugsScreen(patientUuid, facility) }
        }
  }
}
