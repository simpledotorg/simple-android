package org.simple.clinic.search

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent

class PatientSearchScreenControllerTest {

  private val screen: PatientSearchScreen = mock()
  private val repository: PatientRepository = mock()

  private lateinit var controller: PatientSearchScreenController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = PatientSearchScreenController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `search button should remain enabled only when both name and age are present`() {
    uiEvents.onNext(SearchQueryNameChanged("foo"))
    uiEvents.onNext(SearchQueryAgeChanged(" "))

    uiEvents.onNext(SearchQueryNameChanged(""))
    uiEvents.onNext(SearchQueryAgeChanged("123"))

    uiEvents.onNext(SearchQueryNameChanged("bar"))

    verify(screen, times(3)).showSearchButtonAsDisabled()
    verify(screen).showSearchButtonAsEnabled()
  }

  @Test
  fun `when search is clicked with empty name then a validation error should be shown`() {
    // TODO.
  }

  @Test
  fun `when search is clicked with empty age or date of birth then a validation error should be shown`() {
    // TODO.
  }

  @Test
  fun `when search is clicked with empty name or age then patients shouldn't be searched`() {
    uiEvents.onNext(SearchQueryNameChanged("foo"))
    uiEvents.onNext(SearchQueryAgeChanged(" "))
    uiEvents.onNext(SearchClicked())

    uiEvents.onNext(SearchQueryNameChanged(""))
    uiEvents.onNext(SearchQueryAgeChanged("123"))
    uiEvents.onNext(SearchClicked())

    verify(screen, never()).openPatientSearchResultsScreen(any(), any(), any())
  }

  @Test
  fun `when full name and age are present, and search is clicked, matching patients should be shown`() {
    val fullName = "bar"
    val age = "24"

    uiEvents.onNext(SearchQueryNameChanged(fullName))
    uiEvents.onNext(SearchQueryAgeChanged(age))
    uiEvents.onNext(SearchClicked())

    verify(screen).openPatientSearchResultsScreen(fullName, age, "")
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    uiEvents.onNext(SearchQueryDateOfBirthChanged(""))
    uiEvents.onNext(SearchQueryAgeChanged(""))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)

    uiEvents.onNext(SearchQueryAgeChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)

    uiEvents.onNext(SearchQueryAgeChanged(""))
    uiEvents.onNext(SearchQueryDateOfBirthChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)
  }
}
