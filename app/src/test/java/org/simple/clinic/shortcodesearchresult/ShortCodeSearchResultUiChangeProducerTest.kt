package org.simple.clinic.shortcodesearchresult

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class ShortCodeSearchResultUiChangeProducerTest {
  private val ui = mock<ShortCodeSearchResultUi>()
  private val states = PublishSubject.create<ShortCodeSearchResultState>()

  @Before
  fun setup() {
    states
        .compose(ShortCodeSearchResultUiChangeProducer(TrampolineSchedulersProvider()))
        .subscribe { uiChange -> uiChange(ui) }
  }

  @Test
  fun `it shows the loading screen when fetching patients`() {
    // given
    val fetchingPatients = ShortCodeSearchResultState.fetchingPatients("1234567")

    // when
    states.onNext(fetchingPatients)

    // then
    verify(ui).showLoading()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `it shows a list of patients if found`() {
    // given
    val foundPatients = PatientSearchResults(listOf(
        PatientMocker.patientSearchResult(uuid = UUID.fromString("be021029-4935-4400-8e5b-e57176a45517"))), emptyList())

    val patientsFetched = ShortCodeSearchResultState
        .fetchingPatients("1234567")
        .patientsFetched(foundPatients)

    // when
    states.onNext(patientsFetched)

    // then
    verify(ui).hideLoading()
    verify(ui).showSearchResults(foundPatients)
    verify(ui).showSearchPatientButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `it shows a no patients matched when patients are not found`() {
    // given
    val noMatchingPatients = ShortCodeSearchResultState
        .fetchingPatients("1234567")
        .noMatchingPatients()

    // when
    states.onNext(noMatchingPatients)

    // then
    verify(ui).hideLoading()
    verify(ui).showNoPatientsMatched()
    verify(ui).showSearchPatientButton()
    verifyNoMoreInteractions(ui)
  }
}
