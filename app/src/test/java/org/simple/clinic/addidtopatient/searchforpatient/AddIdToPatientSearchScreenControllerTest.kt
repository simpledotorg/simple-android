package org.simple.clinic.addidtopatient.searchforpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class AddIdToPatientSearchScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen: AddIdToPatientSearchScreen = mock()

  private val controller = AddIdToPatientSearchScreenController()
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when search is clicked with empty name then a validation error should be shown`() {
    uiEvents.onNext(SearchQueryNameChanged(""))
    uiEvents.onNext(SearchClicked)

    verify(screen).setEmptyFullNameErrorVisible(true)
  }

  @Test
  fun `when name changes then any validation error on name should be removed`() {
    uiEvents.onNext(SearchQueryNameChanged("Anish"))
    uiEvents.onNext(SearchQueryNameChanged("Anish Acharya"))

    verify(screen, times(2)).setEmptyFullNameErrorVisible(false)
  }

  @Test
  fun `when search is clicked with empty name then patients shouldn't be searched`() {
    uiEvents.onNext(SearchQueryNameChanged(""))
    uiEvents.onNext(SearchClicked)

    verify(screen, never()).openAddIdToPatientSearchResultsScreen(any())
  }

  @Test
  fun `when full name is present, and search is clicked, search results screen should open`() {
    val fullName = "bar"

    uiEvents.onNext(SearchQueryNameChanged(fullName))
    uiEvents.onNext(SearchClicked)

    verify(screen).openAddIdToPatientSearchResultsScreen(fullName)
  }

  @Test
  fun `when a patient item is clicked, the patient summary screen should be opened`() {
    val patientUuid = UUID.fromString("7925e13f-3b04-46b0-b685-7005ebb1b6fd")

    // when
    uiEvents.onNext(PatientItemClicked(patientUuid))

    // then
    verify(screen).openPatientSummary(patientUuid)
  }

  @Test
  fun `when the search query is blank, the all patients list must be shown`() {
    // when
    uiEvents.onNext(SearchQueryNameChanged(""))

    // then
    verify(screen).showAllPatientsInFacility()
  }

  @Test
  fun `when the search query is blank, the search button must be hidden`() {
    // when
    uiEvents.onNext(SearchQueryNameChanged(""))

    // then
    verify(screen).hideSearchButton()
  }

  @Test
  fun `when the search query is not blank, the all patients list must be hidden`() {
    // when
    uiEvents.onNext(SearchQueryNameChanged("a"))

    // then
    verify(screen).hideAllPatientsInFacility()
  }

  @Test
  fun `when the search query is not blank, the search button must be shown`() {
    // when
    uiEvents.onNext(SearchQueryNameChanged("a"))

    // then
    verify(screen).showSearchButton()
  }
}
