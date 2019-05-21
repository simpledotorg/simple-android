package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.atLeastOnce
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset.UTC

@RunWith(JUnitParamsRunner::class)
class PatientEntryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val screen = mock<PatientEntryScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val dobValidator = mock<UserInputDateValidator>()
  private val numberValidator = mock<PhoneNumberValidator>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = PatientEntryScreenController(patientRepository, facilityRepository, userSession, dobValidator, numberValidator)
  private val reporter = MockAnalyticsReporter()
  private val initialOngoingPatientEntrySubject = PublishSubject.create<OngoingNewPatientEntry>()

  private lateinit var errorConsumer: (Throwable) -> Unit

  @Before
  fun setUp() {
    whenever(facilityRepository.currentFacility(userSession)).thenReturn(Observable.just(PatientMocker.facility()))
    whenever(dobValidator.dateInUserTimeZone()).thenReturn(LocalDate.now(UTC))
    whenever(patientRepository.ongoingEntry()).thenReturn(initialOngoingPatientEntrySubject.firstOrError())

    errorConsumer = { throw it }

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(screen) }, { e -> errorConsumer(e) })

    Analytics.addReporter(reporter)
  }

  @After
  fun tearDown() {
    Analytics.removeReporter(reporter)
    reporter.clear()
  }

  @Test
  fun `when screen is created then existing data should be pre-filled`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFillFields(OngoingNewPatientEntry(
        address = Address(
            colonyOrVillage = "",
            district = "district",
            state = "state")))
  }

  @Test
  fun `when screen is created with an already present address, the already present address must be used for prefilling`() {
    val address = Address(
        colonyOrVillage = "colony 1",
        district = "district 2",
        state = "state 3"
    )
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry(address = address)))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFillFields(OngoingNewPatientEntry(address = address))
  }

  @Test
  fun `when save button is clicked then a patient record should be created from the form input`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dobValidator.validate(any(), any())).thenReturn(Result.VALID)
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)

    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12/04/1993"))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("colony"))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))
    uiEvents.onNext(PatientEntrySaveClicked())

    verify(patientRepository).saveOngoingEntry(OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Gender.TRANSGENDER),
        address = Address(colonyOrVillage = "colony", district = "district", state = "state"),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890")
    ))
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)

    uiEvents.onNext(PatientDateOfBirthTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)

    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
  }

  @Test
  fun `when both date-of-birth and age fields have text then an assertion error should be thrown`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    errorConsumer = { assertThat(it).isInstanceOf(AssertionError::class.java) }

    uiEvents.onNext(PatientDateOfBirthTextChanged("1"))
    uiEvents.onNext(PatientAgeTextChanged("1"))
  }

  @Test
  fun `while date-of-birth has focus or has some input then date format should be shown in the label`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
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
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dobValidator.validate(any(), any())).thenReturn(Result.VALID)
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)

    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12/04/1993"))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("colony"))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))

    uiEvents.onNext(TheActivityLifecycle.Paused())

    verify(patientRepository).saveOngoingEntry(OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Gender.TRANSGENDER),
        address = Address(colonyOrVillage = "colony", district = "district", state = "state"),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890")
    ))
  }

  @Test
  fun `when save is clicked then user input should be validated`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    uiEvents.onNext(PatientFullNameTextChanged(""))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
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

    whenever(numberValidator.validate("1234", LANDLINE_OR_MOBILE)).thenReturn(LENGTH_TOO_SHORT)
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234"))
    uiEvents.onNext(PatientEntrySaveClicked())

    whenever(numberValidator.validate("1234567890987654", LANDLINE_OR_MOBILE)).thenReturn(LENGTH_TOO_LONG)
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890987654"))
    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, atLeastOnce()).showEmptyFullNameError(true)
    verify(screen, atLeastOnce()).showEmptyDateOfBirthAndAgeError(true)
    verify(screen, atLeastOnce()).showInvalidDateOfBirthError(true)
    verify(screen, atLeastOnce()).showMissingGenderError(true)
    verify(screen, atLeastOnce()).showEmptyColonyOrVillageError(true)
    verify(screen, atLeastOnce()).showEmptyDistrictError(true)
    verify(screen, atLeastOnce()).showEmptyStateError(true)
    verify(screen, atLeastOnce()).showLengthTooShortPhoneNumberError(true)
    verify(screen, atLeastOnce()).showLengthTooLongPhoneNumberError(true)
  }

  @Test
  fun `when input validation fails, the errors must be sent to analytics`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    uiEvents.onNext(PatientFullNameTextChanged(""))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
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
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12/04/1993"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("colony"))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))

    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))

    verify(screen).showEmptyFullNameError(false)
    verify(screen, atLeastOnce()).showEmptyDateOfBirthAndAgeError(false)
    verify(screen, atLeastOnce()).showInvalidDateOfBirthError(false)
    verify(screen, atLeastOnce()).showDateOfBirthIsInFutureError(false)
    verify(screen).showMissingGenderError(false)
    verify(screen, atLeastOnce()).showEmptyColonyOrVillageError(false)
    verify(screen).showEmptyDistrictError(false)
    verify(screen).showEmptyStateError(false)
  }

  // TODO: Write these similarly structured regression tests in a smarter way.

  @Test
  fun `regression test for validations 1`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.MALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 2`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.MALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 3`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 4`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.FEMALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("Colony"))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))

    uiEvents.onNext(PatientEntrySaveClicked())

    verify(screen).openMedicalHistoryEntryScreen()
    verify(patientRepository).saveOngoingEntry(any())
  }

  @Test
  @Parameters(value = ["FEMALE", "MALE", "TRANSGENDER"])
  fun `when gender is selected for the first time then the form should be scrolled to bottom`(gender: Gender) {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientGenderChanged(Just(gender)))
    uiEvents.onNext(PatientGenderChanged(Just(gender)))

    verify(screen, times(1)).scrollFormToBottom()
  }

  @Test
  fun `when validation errors are shown then the form should be scrolled to the first field with error`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientDistrictTextChanged(""))
    uiEvents.onNext(PatientStateTextChanged(""))

    uiEvents.onNext(PatientEntrySaveClicked())

    // This is order dependent because finding the first field
    // with error is only possible once the errors are set.
    val inOrder = inOrder(screen)
    inOrder.verify(screen).showEmptyDistrictError(true)
    inOrder.verify(screen).scrollToFirstFieldWithError()
  }

  @Test
  fun `when the ongoing entry has an identifier it must be retained when accepting input`() {
    val identifier = Identifier(value = "id", type = BpPassport)
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry(identifier = identifier)))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok Kumar"))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("20"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.FEMALE)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("Colony"))
    uiEvents.onNext(PatientDistrictTextChanged("District"))
    uiEvents.onNext(PatientStateTextChanged("State"))
    uiEvents.onNext(PatientEntrySaveClicked())

    val expectedSavedEntry = OngoingNewPatientEntry(
        personalDetails = PersonalDetails(
            fullName = "Ashok Kumar",
            dateOfBirth = null,
            age = "20",
            gender = Gender.FEMALE
        ),
        address = Address(
            colonyOrVillage = "Colony",
            district = "District",
            state = "State"
        ),
        phoneNumber = null,
        identifier = identifier
    )
    verify(patientRepository).saveOngoingEntry(expectedSavedEntry)
  }

  @Test
  fun `when the ongoing patient entry has an identifier, the identifier section must be shown`() {
    initialOngoingPatientEntrySubject.onNext(OngoingNewPatientEntry(identifier = Identifier("id", BpPassport)))

    uiEvents.onNext(ScreenCreated())

    verify(screen).showIdentifierSection()
  }

  @Test
  fun `when the ongoing patient entry does not have an identifier, the identifier section must be hidden`() {
    initialOngoingPatientEntrySubject.onNext(OngoingNewPatientEntry(identifier = null))

    uiEvents.onNext(ScreenCreated())

    verify(screen).hideIdentifierSection()
  }
}
