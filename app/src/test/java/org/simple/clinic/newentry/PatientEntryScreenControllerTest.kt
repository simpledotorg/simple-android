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
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.ZoneOffset.UTC
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale.ENGLISH

@RunWith(JUnitParamsRunner::class)
class PatientEntryScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientEntryUi>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userSession = mock<UserSession>()
  private val dobValidator = UserInputDateValidator(UTC, DateTimeFormatter.ofPattern("dd/MM/yyyy", ENGLISH))
  private val numberValidator = IndianPhoneNumberValidator()
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
    whenever(patientRepository.ongoingEntry()).thenReturn(initialOngoingPatientEntrySubject.firstOrError())

    errorConsumer = { throw it }

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(ui) }, { e -> errorConsumer(e) })

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

    verify(ui).preFillFields(OngoingNewPatientEntry(
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

    verify(ui).preFillFields(OngoingNewPatientEntry(address = address))
  }

  @Test
  fun `when save button is clicked then a patient record should be created from the form input`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(patientRegisteredCount.get()).thenReturn(0)

    with(uiEvents) {
      onNext(FullNameChanged("Ashok"))
      onNext(PhoneNumberChanged("1234567890"))
      onNext(DateOfBirthChanged("12/04/1993"))
      onNext(AgeChanged(""))
      onNext(GenderChanged(Just(Transgender)))
      onNext(ColonyOrVillageChanged("colony"))
      onNext(DistrictChanged("district"))
      onNext(StateChanged("state"))
      onNext(SaveClicked())
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
    whenever(patientRegisteredCount.get()).thenReturn(existingPatientRegisteredCount)

    with(uiEvents) {
      onNext(OngoingPatientEntryChanged(ongoingEntry))
      onNext(SaveClicked())
    }

    verify(patientRepository).saveOngoingEntry(ongoingEntry)
    verify(patientRegisteredCount).set(existingPatientRegisteredCount + 1)
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(AgeChanged(""))
      onNext(DateOfBirthChanged(""))
    }
    verify(ui).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)

    uiEvents.onNext(DateOfBirthChanged("1"))
    verify(ui).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)

    with(uiEvents) {
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("1"))
    }
    verify(ui).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
  }

  @Test
  fun `when both date-of-birth and age fields have text then an assertion error should be thrown`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    errorConsumer = { assertThat(it).isInstanceOf(AssertionError::class.java) }

    with(uiEvents) {
      onNext(DateOfBirthChanged("1"))
      onNext(AgeChanged("1"))
    }
  }

  @Test
  fun `while date-of-birth has focus or has some input then date format should be shown in the label`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(DateOfBirthChanged(""))
      onNext(DateOfBirthFocusChanged(hasFocus = false))
      onNext(DateOfBirthFocusChanged(hasFocus = true))
      onNext(DateOfBirthChanged("1"))
      onNext(DateOfBirthFocusChanged(hasFocus = false))
    }

    verify(ui, times(1)).setShowDatePatternInDateOfBirthLabel(false)
    verify(ui).setShowDatePatternInDateOfBirthLabel(true)
  }

  @Test
  fun `when screen is paused then ongoing patient entry should be saved`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(FullNameChanged("Ashok"))
      onNext(PhoneNumberChanged("1234567890"))
      onNext(DateOfBirthChanged("12/04/1993"))
      onNext(AgeChanged(""))
      onNext(GenderChanged(Just(Transgender)))
      onNext(ColonyOrVillageChanged("colony"))
      onNext(DistrictChanged("district"))
      onNext(StateChanged("state"))

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
      onNext(FullNameChanged(""))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged(""))
      onNext(GenderChanged(None))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged(""))
      onNext(StateChanged(""))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("33/33/3333"))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(AgeChanged(" "))
      onNext(DateOfBirthChanged(""))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("16/07/2018"))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(PhoneNumberChanged("1234"))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(PhoneNumberChanged("1234567890987654"))
      onNext(SaveClicked())
    }

    verify(ui, atLeastOnce()).showEmptyFullNameError(true)
    verify(ui, atLeastOnce()).showEmptyDateOfBirthAndAgeError(true)
    verify(ui, atLeastOnce()).showInvalidDateOfBirthError(true)
    verify(ui, atLeastOnce()).showMissingGenderError(true)
    verify(ui, atLeastOnce()).showEmptyColonyOrVillageError(true)
    verify(ui, atLeastOnce()).showEmptyDistrictError(true)
    verify(ui, atLeastOnce()).showEmptyStateError(true)
    verify(ui, atLeastOnce()).showLengthTooShortPhoneNumberError(true)
    verify(ui, atLeastOnce()).showLengthTooLongPhoneNumberError(true)
  }

  @Test
  fun `when input validation fails, the errors must be sent to analytics`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(FullNameChanged(""))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged(""))
      onNext(GenderChanged(None))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged(""))
      onNext(StateChanged(""))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("33/33/3333"))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(AgeChanged(" "))
      onNext(DateOfBirthChanged(""))
      onNext(SaveClicked())
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("16/07/2018"))
      onNext(SaveClicked())
    }

    val validationErrors = reporter.receivedEvents
        .filter { it.name == "InputValidationError" }

    assertThat(validationErrors).isNotEmpty()
  }

  @Test
  fun `validation errors should be cleared on every input change`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))

    with(uiEvents) {
      onNext(FullNameChanged("Ashok"))
      onNext(PhoneNumberChanged("1234567890"))
      onNext(DateOfBirthChanged("12/04/1993"))
      onNext(GenderChanged(Just(Transgender)))
      onNext(ColonyOrVillageChanged("colony"))
      onNext(DistrictChanged("district"))
      onNext(StateChanged("state"))

      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(ColonyOrVillageChanged(""))
    }

    verify(ui).showEmptyFullNameError(false)
    verify(ui, atLeastOnce()).showEmptyDateOfBirthAndAgeError(false)
    verify(ui, atLeastOnce()).showInvalidDateOfBirthError(false)
    verify(ui, atLeastOnce()).showDateOfBirthIsInFutureError(false)
    verify(ui).showMissingGenderError(false)
    verify(ui, atLeastOnce()).showEmptyColonyOrVillageError(false)
    verify(ui).showEmptyDistrictError(false)
    verify(ui).showEmptyStateError(false)
  }

  // TODO: Write these similarly structured regression tests in a smarter way.

  @Test
  fun `regression test for validations 1`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Just(Male)))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked())
    }

    verify(ui, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 2`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Just(Male)))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked())
    }

    verify(ui, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 3`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(None))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked())
    }

    verify(ui, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 4`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(patientRegisteredCount.get()).thenReturn(0)

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Just(Female)))
      onNext(ColonyOrVillageChanged("Colony"))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked())
    }

    verify(ui).openMedicalHistoryEntryScreen()
    verify(patientRepository).saveOngoingEntry(any())
    verify(patientRegisteredCount).set(any())
  }

  @Test
  @Parameters(method = "params for gender values")
  fun `when gender is selected for the first time then the form should be scrolled to bottom`(gender: Gender) {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry()))
    with(uiEvents) {
      onNext(GenderChanged(None))
      onNext(GenderChanged(Just(gender)))
      onNext(GenderChanged(Just(gender)))
    }

    verify(ui, times(1)).scrollFormToBottom()
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
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Just(Transgender)))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged(""))
      onNext(StateChanged(""))

      onNext(SaveClicked())
    }

    // This is order dependent because finding the first field
    // with error is only possible once the errors are set.
    val inOrder = inOrder(ui)
    inOrder.verify(ui).showEmptyDistrictError(true)
    inOrder.verify(ui).scrollToFirstFieldWithError()
  }

  @Test
  fun `when the ongoing entry has an identifier it must be retained when accepting input`() {
    val identifier = Identifier(value = "id", type = BpPassport)
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingNewPatientEntry(identifier = identifier)))
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(patientRegisteredCount.get()).thenReturn(0)

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Just(Female)))
      onNext(ColonyOrVillageChanged("Colony"))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))
      onNext(SaveClicked())
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

    verify(ui).showIdentifierSection()
  }

  @Test
  fun `when the ongoing patient entry does not have an identifier, the identifier section must be hidden`() {
    initialOngoingPatientEntrySubject.onNext(OngoingNewPatientEntry(identifier = null))

    uiEvents.onNext(ScreenCreated())

    verify(ui).hideIdentifierSection()
  }
}
