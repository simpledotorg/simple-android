package org.simple.clinic.shortcodesearchresult

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class ShortCodeSearchResultStateProducerTest {
  private val uiEventsSubject = PublishSubject.create<UiEvent>()
  private val patientRepository = mock<PatientRepository>()
  private val shortCode = "1234567"
  private val fetchingPatientsState = ShortCodeSearchResultState.fetchingPatients(shortCode)
  private val ui = mock<ShortCodeSearchResultUi>()
  private val uiStateProducer = ShortCodeSearchResultStateProducer(fetchingPatientsState, patientRepository, ui)
  private val uiStates = uiEventsSubject
      .compose(uiStateProducer)
      .doOnNext { uiStateProducer.states.onNext(it) }

  @Test
  fun `when the screen is created, then patients matching the BP passport number must be fetched`() {
    // given
    val patientSearchResults = listOf(PatientMocker.patientSearchResult())
    whenever(patientRepository.searchByShortCode(shortCode))
        .thenReturn(Observable.just(patientSearchResults))

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(ScreenCreated())

    // then
    testObserver
        .assertNoErrors()
        .assertValues(fetchingPatientsState, fetchingPatientsState.patientsFetched(patientSearchResults))
        .assertNotTerminated()
  }

  @Test
  fun `when the screen is created and there are no patients, then don't show any patients`() {
    // given
    val emptyPatientSearchResults = emptyList<PatientSearchResult>()
    whenever(patientRepository.searchByShortCode(shortCode))
        .thenReturn(Observable.just(emptyPatientSearchResults))

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(ScreenCreated())

    // then
    testObserver
        .assertNoErrors()
        .assertValues(fetchingPatientsState, fetchingPatientsState.noMatchingPatients())
        .assertNotTerminated()
  }

  @Test
  fun `when the user clicks on a patient search result, then open the patient summary screen`() {
    // given
    val patientUuid = UUID.fromString("d18fa4dc-3b47-4a88-826f-342401527d65")
    val patientSearchResults = listOf(PatientMocker.patientSearchResult(uuid = patientUuid))
    val patientsFetched = ShortCodeSearchResultState
        .fetchingPatients("1234567")
        .patientsFetched(patientSearchResults)
    uiStateProducer.states.onNext(patientsFetched) // TODO Fix `setState` in tests

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(ViewPatient(patientUuid))

    // then
    testObserver
        .assertNoErrors()
        .assertNoValues()
        .assertNotTerminated()

    verify(ui).openPatientSummary(patientUuid)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when enter patient name is clicked, then take the user to search patient screen`() {
    // given
    val patientSearchResults = listOf(PatientMocker.patientSearchResult())
    val patientsFetched = ShortCodeSearchResultState
        .fetchingPatients("1234567")
        .patientsFetched(patientSearchResults)
    uiStateProducer.states.onNext(patientsFetched) // TODO Fix `setState` in tests

    val testObserver = uiStates.test()

    // when
    uiEventsSubject.onNext(SearchPatient)

    // then
    testObserver
        .assertNoErrors()
        .assertNoValues()
        .assertNotTerminated()

    verify(ui).openPatientSearch()
    verifyNoMoreInteractions(ui)
  }
}
