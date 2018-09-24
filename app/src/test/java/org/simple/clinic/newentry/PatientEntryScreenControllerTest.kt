package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingPatientEntry
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class PatientEntryScreenControllerTest {

  private val screen = mock<PatientEntryScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val dobValidator = mock<DateOfBirthFormatValidator>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = PatientEntryScreenController(patientRepository, facilityRepository, userSession, dobValidator)
  private val reporter = MockReporter()

  private lateinit var errorConsumer: (Throwable) -> Unit

  @Before
  fun setUp() {
    whenever(facilityRepository.currentFacility(userSession)).thenReturn(Observable.just(PatientMocker.facility()))

    errorConsumer = { throw it }

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(screen) }, { e -> errorConsumer(e) })
    Analytics.addReporter(reporter)
  }

  @Test
  fun `when screen is created then existing data should be pre-filled`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFillFields(OngoingPatientEntry(
        address = OngoingPatientEntry.Address(
            colonyOrVillage = null,
            district = "district",
            state = "state")))
  }

  @Test
  fun `when save button is clicked then a patient record should be created from the form input`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dobValidator.validate(any(), any())).thenReturn(Result.VALID)

    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12/04/1993"))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = true))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))
    uiEvents.onNext(PatientEntrySaveClicked())

    verify(patientRepository).saveOngoingEntry(OngoingPatientEntry(
        personalDetails = OngoingPatientEntry.PersonalDetails("Ashok", "12/04/1993", age = null, gender = Gender.TRANSGENDER),
        address = OngoingPatientEntry.Address(colonyOrVillage = null, district = "district", state = "state"),
        phoneNumber = OngoingPatientEntry.PhoneNumber("1234567890")
    ))
  }

  @Test
  fun `when none is selected then their associated fields should be reset`() {
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = true))

    verify(screen).resetPhoneNumberField()
    verify(screen).resetColonyOrVillageField()
  }

  @Test
  fun `none checkboxes should only be visible while their associated fields are empty`() {
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1"))
    uiEvents.onNext(PatientPhoneNumberTextChanged("12"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))

    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("C"))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("Co"))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))

    verify(screen, times(2)).setNoPhoneNumberCheckboxVisible(true)
    verify(screen, times(1)).setNoPhoneNumberCheckboxVisible(false)

    verify(screen, times(2)).setNoVillageOrColonyCheckboxVisible(true)
    verify(screen, times(1)).setNoVillageOrColonyCheckboxVisible(false)
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)

    uiEvents.onNext(PatientDateOfBirthTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)

    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
  }

  @Test()
  fun `when both date-of-birth and age fields have text then an assertion error should be thrown`() {
    errorConsumer = { assertThat(it).isInstanceOf(AssertionError::class.java) }

    uiEvents.onNext(PatientDateOfBirthTextChanged("1"))
    uiEvents.onNext(PatientAgeTextChanged("1"))
  }

  @Test
  fun `while date-of-birth has focus or has some input then date format should be shown in the label`() {
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthFocusChanged(hasFocus = false))
    uiEvents.onNext(PatientDateOfBirthFocusChanged(hasFocus = true))
    uiEvents.onNext(PatientDateOfBirthTextChanged("1"))
    uiEvents.onNext(PatientDateOfBirthFocusChanged(hasFocus = false))

    verify(screen, times(1)).setShowDatePatternInDateOfBirthLabel(false)
    verify(screen).setShowDatePatternInDateOfBirthLabel(true)
  }

  @Test
  fun `when screen is paused then ongoing patient entry should be saved`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dobValidator.validate(any(), any())).thenReturn(Result.VALID)

    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12/04/1993"))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = false))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))

    uiEvents.onNext(TheActivityLifecycle.Paused())

    verify(patientRepository).saveOngoingEntry(OngoingPatientEntry(
        personalDetails = OngoingPatientEntry.PersonalDetails("Ashok", "12/04/1993", age = null, gender = Gender.TRANSGENDER),
        address = OngoingPatientEntry.Address(colonyOrVillage = null, district = "district", state = "state"),
        phoneNumber = OngoingPatientEntry.PhoneNumber("1234567890")
    ))
  }

  @Test
  fun `when save is clicked then user input should be validated`() {
    uiEvents.onNext(PatientFullNameTextChanged(""))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = false))
    uiEvents.onNext(PatientDistrictTextChanged(""))
    uiEvents.onNext(PatientStateTextChanged(""))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(dobValidator.validate("33/33/3333")).thenReturn(Result.DATE_IS_IN_FUTURE)
    uiEvents.onNext(PatientDateOfBirthTextChanged("33/33/3333"))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(dobValidator.validate(" ")).thenReturn(Result.INVALID_PATTERN)
    uiEvents.onNext(PatientAgeTextChanged(" "))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(dobValidator.validate("16/07/2018")).thenReturn(Result.INVALID_PATTERN)
    uiEvents.onNext(PatientDateOfBirthTextChanged("16/07/2018"))
    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, times(4)).showEmptyFullNameError(true)
    verify(screen, times(4)).showEmptyPhoneNumberError(true)
    verify(screen, times(2)).showEmptyDateOfBirthAndAgeError(true)
    verify(screen).showInvalidDateOfBirthError(true)
    verify(screen, times(4)).showMissingGenderError(true)
    verify(screen, times(4)).showEmptyColonyOrVillageError(true)
    verify(screen, times(4)).showEmptyDistrictError(true)
    verify(screen, times(4)).showEmptyStateError(true)
  }

  @Test
  fun `when input validation fails, the errors must be sent to analytics`() {
    uiEvents.onNext(PatientFullNameTextChanged(""))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = false))
    uiEvents.onNext(PatientDistrictTextChanged(""))
    uiEvents.onNext(PatientStateTextChanged(""))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(dobValidator.validate("33/33/3333")).thenReturn(Result.DATE_IS_IN_FUTURE)
    uiEvents.onNext(PatientDateOfBirthTextChanged("33/33/3333"))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(dobValidator.validate(" ")).thenReturn(Result.INVALID_PATTERN)
    uiEvents.onNext(PatientAgeTextChanged(" "))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(dobValidator.validate("16/07/2018")).thenReturn(Result.INVALID_PATTERN)
    uiEvents.onNext(PatientDateOfBirthTextChanged("16/07/2018"))
    uiEvents.onNext(PatientEntrySaveClicked())

    val validationErrors = reporter.receivedEvents
        .filter { it.name == "InputValidationError" }

    assertThat(validationErrors).isNotEmpty()
  }

  @Test
  fun `validation errors should be cleared on every input change`() {
    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12/04/1993"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = false))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("colony"))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))

    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = true))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))

    verify(screen).showEmptyFullNameError(false)
    verify(screen, times(4)).showEmptyPhoneNumberError(false)
    verify(screen, times(3)).showEmptyDateOfBirthAndAgeError(false)
    verify(screen, times(2)).showInvalidDateOfBirthError(false)
    verify(screen, times(2)).showDateOfBirthIsInFutureError(false)
    verify(screen).showMissingGenderError(false)
    verify(screen, times(4)).showEmptyColonyOrVillageError(false)
    verify(screen).showEmptyDistrictError(false)
    verify(screen).showEmptyStateError(false)
  }

  // TODO: Write these similarly structured regression tests in a smarter way.

  @Test
  fun `regression test for validations 1`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.MALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = false))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 2`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.MALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = false))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 3`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = true))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 4`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.FEMALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = true))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen).openMedicalHistoryEntryScreen()
    verify(patientRepository).saveOngoingEntry(any())
  }

  @Test
  @Parameters(value = ["FEMALE", "MALE", "TRANSGENDER"])
  fun `when gender is selected for the first time then the form should be scrolled to bottom`(gender: Gender) {
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientGenderChanged(Just(gender)))
    uiEvents.onNext(PatientGenderChanged(Just(gender)))

    verify(screen, times(1)).scrollFormToBottom()
  }

  @Test
  fun `when validation errors are shown then the form should be scrolled to the first field with error`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientNoColonyOrVillageToggled(noneSelected = true))
    uiEvents.onNext(PatientDistrictTextChanged(""))
    uiEvents.onNext(PatientStateTextChanged(""))

    uiEvents.onNext(PatientEntrySaveClicked())

    // This is order dependent because finding the first field
    // with error is only possible once the errors are set.
    val inOrder = inOrder(screen)
    inOrder.verify(screen).showEmptyDistrictError(true)
    inOrder.verify(screen).scrollToFirstFieldWithError()
  }

  @After
  fun tearDown() {
    Analytics.removeReporter(reporter)
    reporter.clearReceivedEvents()
  }
}
