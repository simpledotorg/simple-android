package org.simple.clinic.shortcodesearchresult

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.searchresultsview.PatientSearchResults
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class ShortCodeSearchResultUiChangeProducerTest {
  private val ui = mock<ShortCodeSearchResultUi>()
  private val states = PublishSubject.create<ShortCodeSearchResultState>()

  private lateinit var stateSubscription: Disposable

  @After
  fun tearDown() {
    stateSubscription.dispose()
  }

  @Test
  fun `it shows the loading screen when fetching patients`() {
    // given
    val fetchingPatients = ShortCodeSearchResultState.fetchingPatients("1234567")

    // when
    setupUiChangeProducer()
    states.onNext(fetchingPatients)

    // then
    verify(ui).showLoading()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `it shows a list of patients if found`() {
    // given
    val foundPatients = PatientSearchResults(listOf(
        TestData.patientSearchResult(uuid = UUID.fromString("be021029-4935-4400-8e5b-e57176a45517"))), emptyList(), TestData.facility())

    val patientsFetched = ShortCodeSearchResultState
        .fetchingPatients("1234567")
        .patientsFetched(foundPatients)

    // when
    setupUiChangeProducer()
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
    setupUiChangeProducer()
    states.onNext(noMatchingPatients)

    // then
    verify(ui).hideLoading()
    verify(ui).showNoPatientsMatched()
    verify(ui).showSearchPatientButton()
    verifyNoMoreInteractions(ui)
  }

  private fun setupUiChangeProducer() {
    stateSubscription = states
        .compose(ShortCodeSearchResultUiChangeProducer(TrampolineSchedulersProvider()))
        .subscribe { uiChange -> uiChange(ui) }
  }
}
