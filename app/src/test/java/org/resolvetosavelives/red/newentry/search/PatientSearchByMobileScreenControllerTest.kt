package org.resolvetosavelives.red.newentry.search

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.widgets.UiEvent

class PatientSearchByMobileScreenControllerTest {

  private val screen: PatientSearchByMobileScreen = mock()
  private val repository: PatientRepository = mock()

  private lateinit var controller: PatientSearchByMobileScreenController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = PatientSearchByMobileScreenController(repository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on mobile number field and patient list should be setup`() {
    verify(screen).showKeyboardOnMobileNumberField()
    verify(screen).setupSearchResultsList()
  }

  @Test
  fun `when mobile number text changes then matching patients should be shown`() {
    val partialNumber = "999"
    val matchingPatients = listOf<Patient>(
        mock(),
        mock(),
        mock())
    whenever(repository.search(partialNumber)).thenReturn(Observable.just(matchingPatients))

    uiEvents.onNext(PatientMobileNumberTextChanged(partialNumber))

    verify(screen).updatePatientSearchResults(matchingPatients)
  }

  @Test
  fun `when new patient is clicked then the manual entry flow should be started`() {
    val partialNumber = "999"
    whenever(repository.search(partialNumber)).thenReturn(Observable.never())
    whenever(repository.save(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientMobileNumberTextChanged(partialNumber))
    uiEvents.onNext(PatientSearchByMobileProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).save(capture())
      assertEquals(partialNumber, firstValue.mobileNumber)
    }
    verify(screen).openPersonalDetailsEntryScreen()
  }
}
