package org.simple.clinic.search.results

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

typealias Ui = PatientSearchResultsUi
typealias UiChange = (Ui) -> Unit

class PatientSearchResultsController @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val facilityRepository: FacilityRepository,
    private val userSession: UserSession,
    @Assisted private val patientSearchCriteria: PatientSearchCriteria
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(patientSearchCriteria: PatientSearchCriteria): PatientSearchResultsController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
