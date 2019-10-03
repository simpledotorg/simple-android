package org.simple.clinic.newentry

import com.f2prateek.rx.preferences2.Preference
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
import org.simple.clinic.activity.TheActivityLifecycle
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
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
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
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
  private val patientRegisteredCount = mock<Preference<Int>>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = PatientEntryScreenController(
      patientRepository,
      facilityRepository,
      userSession,
      dobValidator,
      numberValidator,
      patientRegisteredCount
  )
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
    whenever(dobValidator.validate(any(), any())).thenReturn(Valid(LocalDate.parse("1993-04-12")))
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)
    whenever(patientRegisteredCount.get()).thenReturn(0)

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok"))
      onNext(PatientPhoneNumberTextChanged("1234567890"))
      onNext(PatientDateOfBirthTextChanged("12/04/1993"))
      onNext(PatientAgeTextChanged(""))
      onNext(PatientGenderChanged(Just(Transgender)))
      onNext(PatientColonyOrVillageTextChanged("colony"))
      onNext(PatientDistrictTextChanged("district"))
      onNext(PatientStateTextChanged("state"))
      onNext(PatientEntrySaveClicked())
    }

    verify(patientRepository).saveOngoingEntry(OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Transgender),
        address = Address(colonyOrVillage = "colony", district = "district", state = "state"),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890")
    ))
    verify(patientRegisteredCount).set(1)
  }

  @Test
  fun `when save is clicked and patient is saved then patient registered count should be incremented`() {
    val existingPatientRegisteredCount = 5
    val ongoingEntry = OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Transgender),
        address = Address(colonyOrVillage = "colony", district = "district", state = "state"),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890")
    )

    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dobValidator.validate(any(), any())).thenReturn(Valid(LocalDate.parse("1993-04-12")))
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)
    whenever(patientRegisteredCount.get()).thenReturn(existingPatientRegisteredCount)

    with(uiEvents) {
      onNext(OngoingPatientEntryChanged(ongoingEntry))
      onNext(PatientEntrySaveClicked())
    }

    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(patientRegisteredCount).set(existingPatientRegisteredCount + 1)
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(PatientAgeTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
    }
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)

    uiEvents.onNext(PatientDateOfBirthTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)

    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("1"))
    }
    verify(screen).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
  }

  @Test
  fun `when both date-of-birth and age fields have text then an assertion error should be thrown`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    errorConsumer = { assertThat(it).isInstanceOf(AssertionError::class.java) }

    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged("1"))
      onNext(PatientAgeTextChanged("1"))
    }
  }

  @Test
  fun `while date-of-birth has focus or has some input then date format should be shown in the label`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientDateOfBirthFocusChanged(hasFocus = false))
      onNext(PatientDateOfBirthFocusChanged(hasFocus = true))
      onNext(PatientDateOfBirthTextChanged("1"))
      onNext(PatientDateOfBirthFocusChanged(hasFocus = false))
    }

    verify(screen, times(1)).setShowDatePatternInDateOfBirthLabel(false)
    verify(screen).setShowDatePatternInDateOfBirthLabel(true)
  }

  @Test
  fun `when screen is paused then ongoing patient entry should be saved`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dobValidator.validate(any(), any())).thenReturn(Valid(LocalDate.parse("1993-04-12")))
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok"))
      onNext(PatientPhoneNumberTextChanged("1234567890"))
      onNext(PatientDateOfBirthTextChanged("12/04/1993"))
      onNext(PatientAgeTextChanged(""))
      onNext(PatientGenderChanged(Just(Transgender)))
      onNext(PatientColonyOrVillageTextChanged("colony"))
      onNext(PatientDistrictTextChanged("district"))
      onNext(PatientStateTextChanged("state"))

      onNext(TheActivityLifecycle.Paused())
    }

    verify(patientRepository).saveOngoingEntry(OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Transgender),
        address = Address(colonyOrVillage = "colony", district = "district", state = "state"),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890")
    ))
  }

  @Test
  fun `when save is clicked then user input should be validated`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(PatientFullNameTextChanged(""))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged(""))
      onNext(PatientGenderChanged(None))
      onNext(PatientColonyOrVillageTextChanged(""))
      onNext(PatientDistrictTextChanged(""))
      onNext(PatientStateTextChanged(""))
      onNext(PatientEntrySaveClicked())
    }

    whenever(dobValidator.validate("33/33/3333")).thenReturn(Invalid.DateIsInFuture)
    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged("33/33/3333"))
      onNext(PatientEntrySaveClicked())
    }

    whenever(dobValidator.validate(" ")).thenReturn(Invalid.InvalidPattern)
    with(uiEvents) {
      onNext(PatientAgeTextChanged(" "))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientEntrySaveClicked())
    }

    whenever(dobValidator.validate("16/07/2018")).thenReturn(Invalid.InvalidPattern)
    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged("16/07/2018"))
      onNext(PatientEntrySaveClicked())
    }

    whenever(numberValidator.validate("1234", LANDLINE_OR_MOBILE)).thenReturn(LENGTH_TOO_SHORT)
    with(uiEvents) {
      onNext(PatientPhoneNumberTextChanged("1234"))
      onNext(PatientEntrySaveClicked())
    }

    whenever(numberValidator.validate("1234567890987654", LANDLINE_OR_MOBILE)).thenReturn(LENGTH_TOO_LONG)
    with(uiEvents) {
      onNext(PatientPhoneNumberTextChanged("1234567890987654"))
      onNext(PatientEntrySaveClicked())
    }

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
    with(uiEvents) {
      onNext(PatientFullNameTextChanged(""))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged(""))
      onNext(PatientGenderChanged(None))
      onNext(PatientColonyOrVillageTextChanged(""))
      onNext(PatientDistrictTextChanged(""))
      onNext(PatientStateTextChanged(""))
      onNext(PatientEntrySaveClicked())
    }

    whenever(dobValidator.validate("33/33/3333")).thenReturn(Invalid.DateIsInFuture)
    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged("33/33/3333"))
      onNext(PatientEntrySaveClicked())
    }

    whenever(dobValidator.validate(" ")).thenReturn(Invalid.InvalidPattern)
    with(uiEvents) {
      onNext(PatientAgeTextChanged(" "))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientEntrySaveClicked())
    }

    whenever(dobValidator.validate("16/07/2018")).thenReturn(Invalid.InvalidPattern)
    with(uiEvents) {
      onNext(PatientDateOfBirthTextChanged("16/07/2018"))
      onNext(PatientEntrySaveClicked())
    }

    val validationErrors = reporter.receivedEvents
        .filter { it.name == "InputValidationError" }

    assertThat(validationErrors).isNotEmpty()
  }

  @Test
  fun `validation errors should be cleared on every input change`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok"))
      onNext(PatientPhoneNumberTextChanged("1234567890"))
      onNext(PatientDateOfBirthTextChanged("12/04/1993"))
      onNext(PatientGenderChanged(Just(Transgender)))
      onNext(PatientColonyOrVillageTextChanged("colony"))
      onNext(PatientDistrictTextChanged("district"))
      onNext(PatientStateTextChanged("state"))

      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientColonyOrVillageTextChanged(""))
    }

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

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok Kumar"))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientGenderChanged(Just(Male)))
      onNext(PatientColonyOrVillageTextChanged(""))
      onNext(PatientDistrictTextChanged("District"))
      onNext(PatientStateTextChanged("State"))

      onNext(PatientEntrySaveClicked())
    }

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 2`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok Kumar"))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientGenderChanged(Just(Male)))
      onNext(PatientColonyOrVillageTextChanged(""))
      onNext(PatientDistrictTextChanged("District"))
      onNext(PatientStateTextChanged("State"))

      onNext(PatientEntrySaveClicked())
    }

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 3`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok Kumar"))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientGenderChanged(None))
      onNext(PatientColonyOrVillageTextChanged(""))
      onNext(PatientDistrictTextChanged("District"))
      onNext(PatientStateTextChanged("State"))

      onNext(PatientEntrySaveClicked())
    }

    verify(screen, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 4`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(patientRegisteredCount.get()).thenReturn(0)

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok Kumar"))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientGenderChanged(Just(Female)))
      onNext(PatientColonyOrVillageTextChanged("Colony"))
      onNext(PatientDistrictTextChanged("District"))
      onNext(PatientStateTextChanged("State"))

      onNext(PatientEntrySaveClicked())
    }

    verify(screen).openMedicalHistoryEntryScreen()
    verify(patientRepository).saveOngoingEntry(any())
    verify(patientRegisteredCount).set(any())
  }

  @Test
  @Parameters(method = "params for gender values")
  fun `when gender is selected for the first time then the form should be scrolled to bottom`(gender: Gender) {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(PatientGenderChanged(None))
      onNext(PatientGenderChanged(Just(gender)))
      onNext(PatientGenderChanged(Just(gender)))
    }

    verify(screen, times(1)).scrollFormToBottom()
  }

  @Suppress("Unused")
  private fun `params for gender values`(): List<Gender> {
    return listOf(Male, Female, Transgender)
  }

  @Test
  fun `when validation errors are shown then the form should be scrolled to the first field with error`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok Kumar"))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientGenderChanged(Just(Transgender)))
      onNext(PatientColonyOrVillageTextChanged(""))
      onNext(PatientDistrictTextChanged(""))
      onNext(PatientStateTextChanged(""))

      onNext(PatientEntrySaveClicked())
    }

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
    whenever(patientRegisteredCount.get()).thenReturn(0)

    with(uiEvents) {
      onNext(PatientFullNameTextChanged("Ashok Kumar"))
      onNext(PatientPhoneNumberTextChanged(""))
      onNext(PatientDateOfBirthTextChanged(""))
      onNext(PatientAgeTextChanged("20"))
      onNext(PatientGenderChanged(Just(Female)))
      onNext(PatientColonyOrVillageTextChanged("Colony"))
      onNext(PatientDistrictTextChanged("District"))
      onNext(PatientStateTextChanged("State"))
      onNext(PatientEntrySaveClicked())
    }

    val expectedSavedEntry = OngoingNewPatientEntry(
        personalDetails = PersonalDetails(
            fullName = "Ashok Kumar",
            dateOfBirth = null,
            age = "20",
            gender = Female
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
