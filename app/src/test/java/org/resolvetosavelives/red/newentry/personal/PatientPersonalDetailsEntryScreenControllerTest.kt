package org.resolvetosavelives.red.newentry.personal

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientPersonalDetailsEntryScreenControllerTest {

  private val screen: PatientPersonalDetailsEntryScreen = mock()
  private var repository: PatientRepository = mock()
  private val dummyMobileNumbers = OngoingPatientEntry.MobileNumbers("123", "456")

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private lateinit var controller: PatientPersonalDetailsEntryScreenController

  @Before
  fun setUp() {
    controller = PatientPersonalDetailsEntryScreenController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on full name field`() {
    // This is unrelated to this test, this suppresses an
    // error that happens after this test has passed.
    whenever(repository.ongoingEntry()).thenReturn(Single.never())

    uiEvents.onNext(ScreenCreated())

    verify(screen).showKeyboardOnFullnameField()
  }

  @Test
  fun `when screen starts and existing personal details are present then existing details should be pre-filled`() {
    val existingPersonalDetails = OngoingPatientEntry.PersonalDetails("Ashok kumar", "01/01/1900", 25, Gender.TRANS)
    val existingEntry = OngoingPatientEntry(mobileNumbers = dummyMobileNumbers, personalDetails = existingPersonalDetails)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(existingEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFill(existingPersonalDetails)
  }

  @Test
  fun `when screen starts and existing personal details are not present then existing details should not be pre-filled`() {
    val existingEntry = OngoingPatientEntry(mobileNumbers = dummyMobileNumbers, personalDetails = null)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(existingEntry))

    verify(screen, never()).preFill(any())
  }

  @Test
  fun `when proceed is clicked then personal details should be saved and the next screen should be opened`() {
    val details = OngoingPatientEntry.PersonalDetails("Ashok kumar", "01/01/1900", 25, Gender.TRANS)

    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry(mobileNumbers = dummyMobileNumbers)))
    whenever(repository.save(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged(details.fullName))
    uiEvents.onNext(PatientDateOfBirthTextChanged(details.dateOfBirth))
    uiEvents.onNext(PatientAgeTextChanged(details.ageWhenCreated))
    uiEvents.onNext(PatientGenderChanged(details.gender))
    uiEvents.onNext(PatientPersonalDetailsProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).save(capture())
      assertEquals(details, firstValue.personalDetails)
    }

    verify(screen).openAddressEntryScreen()
  }
}
