package org.simple.clinic.shortcodesearchresult

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

class ShortCodeSearchResultStateProducerTest {
  private val uiEventsSubject = PublishSubject.create<UiEvent>()
  private val patientRepository = mock<PatientRepository>()
  private val shortCode = "1234567"
  private val fetchingPatientsState = ShortCodeSearchResultState.fetchingPatients(shortCode)
  private val uiStateProducer = ShortCodeSearchResultStateProducer(fetchingPatientsState, patientRepository)
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
    with(testObserver) {
      assertNoErrors()
      assertValues(fetchingPatientsState, fetchingPatientsState.patientsFetched(patientSearchResults))
      assertNotTerminated()
    }
  }
}
