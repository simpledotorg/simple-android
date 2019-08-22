package org.simple.clinic.shortcodesearchresult

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.plumbing.BaseUiStateProducer
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class ShortCodeSearchResultStateProducer(
    private val shortCode: String,
    private val patientRepository: PatientRepository,
    private val ui: ShortCodeSearchResultUi,
    private val schedulersProvider: SchedulersProvider
) : BaseUiStateProducer<UiEvent, ShortCodeSearchResultState>() {

  override fun apply(events: Observable<UiEvent>): ObservableSource<ShortCodeSearchResultState> {
    return Observable.merge(
        initialStates(events),
        fetchPatients(events),
        viewPatient(events),
        searchPatient(events)
    )
  }

  private fun initialStates(events: Observable<UiEvent>) = events.ofType<ScreenCreated>()
      .map { ShortCodeSearchResultState.fetchingPatients(shortCode) }

  private fun fetchPatients(events: Observable<UiEvent>): Observable<ShortCodeSearchResultState> {
    return events.ofType<ScreenCreated>()
        .flatMap {
          patientRepository
              .searchByShortCode(shortCode)
              .subscribeOn(schedulersProvider.io())
              .withLatestFrom(states) { patientSearchResults, state ->
                if (patientSearchResults.isNotEmpty()) {
                  state.patientsFetched(patientSearchResults)
                } else {
                  state.noMatchingPatients()
                }
              }
        }
  }

  private fun viewPatient(events: Observable<UiEvent>): Observable<ShortCodeSearchResultState> {
    return events.ofType<ViewPatient>()
        .doOnNext { ui.openPatientSummary(it.patientUuid) }
        .flatMap { Observable.empty<ShortCodeSearchResultState>() }
  }

  private fun searchPatient(events: Observable<UiEvent>): Observable<ShortCodeSearchResultState> {
    return events.ofType<SearchPatient>()
        .doOnNext { ui.openPatientSearch() }
        .flatMap { Observable.empty<ShortCodeSearchResultState>() }
  }
}
