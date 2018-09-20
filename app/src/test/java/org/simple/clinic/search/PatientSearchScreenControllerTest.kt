package org.simple.clinic.search

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.simple.clinic.newentry.DateOfBirthAndAgeVisibility
import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.widgets.UiEvent

class PatientSearchScreenControllerTest {

  private val screen: PatientSearchScreen = mock()
  private val repository: PatientRepository = mock()
  private val dateValidator: DateOfBirthFormatValidator = mock()

  private lateinit var controller: PatientSearchScreenController
  private val uiEvents = PublishSubject.create<UiEvent>()

  @Before
  fun setUp() {
    controller = PatientSearchScreenController(repository, dateValidator)

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
    val validDob = "12/01/1992"
    whenever(dateValidator.validate(validDob)).thenReturn(Result.VALID)

    uiEvents.onNext(SearchQueryNameChanged(""))
    uiEvents.onNext(SearchQueryAgeChanged(""))
    uiEvents.onNext(SearchQueryDateOfBirthChanged(""))

    uiEvents.onNext(SearchQueryAgeChanged("23"))
    uiEvents.onNext(SearchClicked())

    uiEvents.onNext(SearchQueryAgeChanged(""))
    uiEvents.onNext(SearchQueryDateOfBirthChanged(validDob))
    uiEvents.onNext(SearchClicked())

    verify(screen, times(2)).setEmptyFullNameErrorVisible(true)
  }

  @Test
  fun `when search is clicked with empty age or date of birth then a validation error should be shown`() {
    val validDob = "12/01/1992"
    whenever(dateValidator.validate(validDob)).thenReturn(Result.VALID)

    uiEvents.onNext(SearchQueryNameChanged("Anish Acharya"))
    uiEvents.onNext(SearchQueryAgeChanged(""))
    uiEvents.onNext(SearchQueryDateOfBirthChanged(""))

    uiEvents.onNext(SearchQueryAgeChanged("23"))
    uiEvents.onNext(SearchQueryAgeChanged(""))
    uiEvents.onNext(SearchClicked())

    uiEvents.onNext(SearchQueryDateOfBirthChanged(validDob))
    uiEvents.onNext(SearchQueryDateOfBirthChanged(""))
    uiEvents.onNext(SearchClicked())

    verify(screen, times(2)).setEmptyDateOfBirthAndAgeErrorVisible(true)
  }

  @Test
  fun `when search is clicked with an invalid date of birth then a validation error should be shown`() {
    val validDob = "12/01/1993"
    val invalidPattern = "01/13/2012"
    val futureDate = "12/01/3333"

    whenever(dateValidator.validate(validDob)).thenReturn(Result.VALID)
    whenever(dateValidator.validate(invalidPattern)).thenReturn(Result.INVALID_PATTERN)
    whenever(dateValidator.validate(futureDate)).thenReturn(Result.DATE_IS_IN_FUTURE)

    uiEvents.onNext(SearchQueryNameChanged("Anish Acharya"))
    uiEvents.onNext(SearchQueryAgeChanged(""))

    uiEvents.onNext(SearchQueryDateOfBirthChanged(invalidPattern))
    uiEvents.onNext(SearchClicked())

    uiEvents.onNext(SearchQueryDateOfBirthChanged(futureDate))
    uiEvents.onNext(SearchClicked())

    verify(screen).setInvalidDateOfBirthErrorVisible(true)
    verify(screen).setDateOfBirthIsInFutureErrorVisible(true)
  }

  @Test
  fun `when name changes then any validation error on name should be removed`() {
    uiEvents.onNext(SearchQueryNameChanged("Anish"))
    uiEvents.onNext(SearchQueryNameChanged("Anish Acharya"))

    verify(screen, times(2)).setEmptyFullNameErrorVisible(false)
  }

  @Test
  fun `when age changes then any validation error on age should be removed`() {
    uiEvents.onNext(SearchQueryAgeChanged("2"))
    uiEvents.onNext(SearchQueryAgeChanged("23"))

    verify(screen, times(2)).setEmptyDateOfBirthAndAgeErrorVisible(false)
  }

  @Test
  fun `when date of birth changes then any validation error on date of birth should be removed`() {
    uiEvents.onNext(SearchQueryDateOfBirthChanged("12/"))
    uiEvents.onNext(SearchQueryDateOfBirthChanged("12/01/"))

    verify(screen, times(2)).setEmptyDateOfBirthAndAgeErrorVisible(false)
    verify(screen, times(2)).setInvalidDateOfBirthErrorVisible(false)
    verify(screen, times(2)).setDateOfBirthIsInFutureErrorVisible(false)
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
  fun `when full name and age are present, and search is clicked, search results screen should open`() {
    val fullName = "bar"
    val age = "24"

    uiEvents.onNext(SearchQueryNameChanged(fullName))
    uiEvents.onNext(SearchQueryAgeChanged(age))
    uiEvents.onNext(SearchQueryDateOfBirthChanged(""))
    uiEvents.onNext(SearchClicked())

    verify(screen).openPatientSearchResultsScreen(fullName, age, "")
  }

  @Test
  fun `when full name and date of birth are present, and search is clicked, search results screen should open`() {
    val fullName = "bar"
    val dateOfBirth = "24/04/1992"
    whenever(dateValidator.validate(dateOfBirth)).thenReturn(Result.VALID)

    uiEvents.onNext(SearchQueryNameChanged(fullName))
    uiEvents.onNext(SearchQueryAgeChanged(""))
    uiEvents.onNext(SearchQueryDateOfBirthChanged(dateOfBirth))
    uiEvents.onNext(SearchClicked())

    verify(screen).openPatientSearchResultsScreen(fullName, "", dateOfBirth)
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
