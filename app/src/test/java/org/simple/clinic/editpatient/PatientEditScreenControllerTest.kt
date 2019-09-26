package org.simple.clinic.editpatient

import com.google.common.truth.Truth
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.editpatient.PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.AGE_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.BOTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var screen: PatientEditScreen
  private lateinit var patientRepository: PatientRepository
  private lateinit var errorConsumer: (Throwable) -> Unit

  private val utcClock: TestUtcClock = TestUtcClock()
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  @Before
  fun setUp() {
    screen = mock()
    patientRepository = mock()

    val controller = PatientEditScreenController(
        patientRepository,
        IndianPhoneNumberValidator(),
        utcClock,
        TestUserClock(),
        UserInputDateValidator(ZoneOffset.UTC, dateOfBirthFormat),
        dateOfBirthFormat)

    errorConsumer = { throw it }

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(screen) }, { e -> errorConsumer(e) })
  }

  @Test
  fun `when save is clicked, patient name should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditPatientNameTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(FULL_NAME_EMPTY))
  }

  @Test
  fun `when save is clicked, the colony should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Male))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditColonyOrVillageChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(COLONY_OR_VILLAGE_EMPTY))
  }

  @Test
  fun `when save is clicked, the district should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditDistrictTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(DISTRICT_EMPTY))
  }

  @Test
  fun `when save is clicked, the state should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditStateTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(STATE_EMPTY))
  }

  @Test
  fun `when save is clicked, the age should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))
    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditAgeTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT))
  }

  @Test
  @Parameters(method = "params for hiding errors on text changes")
  fun `when input changes, errors corresponding to the input must be hidden`(textChangeParams: HidingErrorsOnTextChangeParams) {
    val (inputChange, expectedErrorsToHide) = textChangeParams

    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))
    uiEvents.onNext(PatientEditSaveClicked())
    uiEvents.onNext(inputChange)

    if (expectedErrorsToHide.isNotEmpty()) {
      verify(screen).hideValidationErrors(expectedErrorsToHide)
    } else {
      verify(screen, never()).hideValidationErrors(any())
    }
  }

  @Suppress("Unused")
  private fun `params for hiding errors on text changes`(): List<HidingErrorsOnTextChangeParams> {
    return listOf(
        HidingErrorsOnTextChangeParams(PatientEditPatientNameTextChanged(""), setOf(FULL_NAME_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditPatientNameTextChanged("Name"), setOf(FULL_NAME_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditPhoneNumberTextChanged(""), setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, PHONE_NUMBER_LENGTH_TOO_LONG)),
        HidingErrorsOnTextChangeParams(PatientEditPhoneNumberTextChanged("12345"), setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, PHONE_NUMBER_LENGTH_TOO_LONG)),
        HidingErrorsOnTextChangeParams(PatientEditColonyOrVillageChanged(""), setOf(COLONY_OR_VILLAGE_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditColonyOrVillageChanged("Colony"), setOf(COLONY_OR_VILLAGE_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditStateTextChanged(""), setOf(STATE_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditStateTextChanged("State"), setOf(STATE_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditDistrictTextChanged(""), setOf(DISTRICT_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditDistrictTextChanged("District"), setOf(DISTRICT_EMPTY)),
        HidingErrorsOnTextChangeParams(PatientEditAgeTextChanged("1"), setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT)),
        HidingErrorsOnTextChangeParams(PatientEditDateOfBirthTextChanged("20/02/1990"), setOf(DATE_OF_BIRTH_IN_FUTURE, INVALID_DATE_OF_BIRTH)),
        HidingErrorsOnTextChangeParams(PatientEditGenderChanged(Transgender), emptySet())
    )
  }

  @Test
  fun `when data of birth has focus, the date format should be shown in the label`() {
    uiEvents.onNext(PatientEditDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientEditDateOfBirthFocusChanged(hasFocus = true))
    uiEvents.onNext(PatientEditDateOfBirthFocusChanged(hasFocus = false))
    uiEvents.onNext(PatientEditDateOfBirthFocusChanged(hasFocus = true))
    uiEvents.onNext(PatientEditDateOfBirthFocusChanged(hasFocus = false))

    verify(screen, times(2)).showDatePatternInDateOfBirthLabel()
    verify(screen, times(2)).hideDatePatternInDateOfBirthLabel()
  }

  @Test
  fun `when date of birth text changes, the date format should be shown in the label`() {
    uiEvents.onNext(PatientEditDateOfBirthFocusChanged(hasFocus = false))
    uiEvents.onNext(PatientEditDateOfBirthTextChanged("01/01/1990"))
    uiEvents.onNext(PatientEditDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientEditDateOfBirthTextChanged("01/01/1990"))

    verify(screen, times(2)).showDatePatternInDateOfBirthLabel()
    verify(screen).hideDatePatternInDateOfBirthLabel()
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    uiEvents.onNext(PatientEditAgeTextChanged(""))
    uiEvents.onNext(PatientEditDateOfBirthTextChanged(""))
    verify(screen).setDateOfBirthAndAgeVisibility(BOTH_VISIBLE)

    uiEvents.onNext(PatientEditDateOfBirthTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(DATE_OF_BIRTH_VISIBLE)

    uiEvents.onNext(PatientEditDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))
    verify(screen).setDateOfBirthAndAgeVisibility(AGE_VISIBLE)
  }

  @Test
  fun `when both date-of-birth and age fields have text then an assertion error should be thrown`() {
    errorConsumer = { Truth.assertThat(it).isInstanceOf(AssertionError::class.java) }

    uiEvents.onNext(PatientEditDateOfBirthTextChanged("1"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))
  }

  @Test
  @Parameters(method = "params for confirming discard changes")
  fun `when back is clicked, the confirm discard changes popup must be shown if there have been changes`(testParams: ConfirmDiscardChangesTestParams) {
    val (existingSavedPatient,
        existingSavedAddress,
        existingSavedPhoneNumber,
        inputEvents,
        shouldShowConfirmDiscardChangesPopup
    ) = testParams

    uiEvents.onNext(PatientEditScreenCreated.from(existingSavedPatient, existingSavedAddress, existingSavedPhoneNumber))
    inputEvents.forEach { uiEvents.onNext(it) }
    uiEvents.onNext(PatientEditBackClicked())

    if (shouldShowConfirmDiscardChangesPopup) {
      verify(screen).showDiscardChangesAlert()
      verify(screen, never()).goBack()
    } else {
      verify(screen).goBack()
      verify(screen, never()).showDiscardChangesAlert()
    }
  }

  @Suppress("Unused")
  private fun `params for confirming discard changes`(): List<ConfirmDiscardChangesTestParams> {
    val noUserInputOnScreen = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("b50f8631-aa87-4bad-989e-552c3c36bb60"),
            addressUuid = UUID.fromString("399f6d5c-b62a-49e7-b1c5-fedbf8be4b0a")),
        inputEvents = emptyList(),
        shouldShowConfirmDiscardChangesPopup = false)

    val allFieldsChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("1ed5d944-1cf8-4a2d-8324-f347255236ed"),
            addressUuid = UUID.fromString("80af8f3f-309a-4624-ae3f-3f8ab1e9ef55"),
            name = "Anish",
            phoneNumber = "123456",
            gender = Female,
            colonyOrVillage = "Bathinda",
            district = "Hoshiarpur",
            state = "Bengaluru",
            ageValue = 30),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditGenderChanged(Transgender),
            PatientEditColonyOrVillageChanged("Batinda"),
            PatientEditDistrictTextChanged("Hosiarpur"),
            PatientEditStateTextChanged("Bangalore"),
            PatientEditAgeTextChanged("32")),
        shouldShowConfirmDiscardChangesPopup = true)

    val allFieldsChangedAndAgeConvertedToDateOfBirth = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("a1b9fba3-8b98-4503-aa7d-10f2f5640892"),
            addressUuid = UUID.fromString("e3a3ad09-ba97-4f40-9787-e942e1ac09b9"),
            name = "Anish",
            phoneNumber = "123456",
            gender = Female,
            colonyOrVillage = "Bathinda",
            district = "Hoshiarpur",
            state = "Bengaluru",
            ageValue = 30),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditGenderChanged(Transgender),
            PatientEditColonyOrVillageChanged("Batinda"),
            PatientEditDistrictTextChanged("Hosiarpur"),
            PatientEditStateTextChanged("Bangalore"),
            PatientEditAgeTextChanged(""),
            PatientEditDateOfBirthTextChanged("13/06/1995")),
        shouldShowConfirmDiscardChangesPopup = true)

    val allFieldsChangedAndDateOfBirthSeparatorsChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("f36e29c3-07ff-44d9-aff0-81cb5f104023"),
            addressUuid = UUID.fromString("197fc38c-f3cd-428f-b0d3-7bd4e076add6"),
            name = "Anish",
            phoneNumber = "123456",
            gender = Female,
            colonyOrVillage = "Bathinda",
            district = "Hoshiarpur",
            state = "Bengaluru",
            dateOfBirthString = "1995-06-13"),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditGenderChanged(Transgender),
            PatientEditColonyOrVillageChanged("Batinda"),
            PatientEditDistrictTextChanged("Hosiarpur"),
            PatientEditStateTextChanged("Bangalore"),
            PatientEditDateOfBirthTextChanged("13/06/1994")),
        shouldShowConfirmDiscardChangesPopup = true)

    val allFieldsChangedAndDateOfBirthConvertedToAge = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("2b3d15bb-c7ed-4bb4-807d-a4697d47f3b3"),
            addressUuid = UUID.fromString("2ca09c58-99d4-4465-bebf-025b3e7f9326"),
            name = "Anish",
            phoneNumber = "123456",
            gender = Female,
            colonyOrVillage = "Bathinda",
            district = "Hoshiarpur",
            state = "Bengaluru",
            dateOfBirthString = "1995-06-13"),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditGenderChanged(Transgender),
            PatientEditColonyOrVillageChanged("Batinda"),
            PatientEditDistrictTextChanged("Hosiarpur"),
            PatientEditStateTextChanged("Bangalore"),
            PatientEditDateOfBirthTextChanged(""),
            PatientEditAgeTextChanged("30")),
        shouldShowConfirmDiscardChangesPopup = true)

    val allFieldsEditedButRevertedBackToOriginalValues = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("4a015332-a9f7-4d1b-bd79-3899c5f3c1b5"),
            addressUuid = UUID.fromString("4a477633-79eb-4bd0-beb5-13957b01ea7a"),
            name = "Anish",
            phoneNumber = "123456",
            gender = Female,
            colonyOrVillage = "Bathinda",
            district = "Hoshiarpur",
            state = "Bengaluru",
            ageValue = 30),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditGenderChanged(Transgender),
            PatientEditColonyOrVillageChanged("Batinda"),
            PatientEditDistrictTextChanged("Hosiarpur"),
            PatientEditStateTextChanged("Bangalore"),
            PatientEditAgeTextChanged("31"),
            PatientEditPatientNameTextChanged("Anish"),
            PatientEditPhoneNumberTextChanged("123456"),
            PatientEditGenderChanged(Female),
            PatientEditColonyOrVillageChanged("Bathinda"),
            PatientEditDistrictTextChanged("Hoshiarpur"),
            PatientEditStateTextChanged("Bengaluru"),
            PatientEditAgeTextChanged("30")),
        shouldShowConfirmDiscardChangesPopup = false)

    val allFieldsEditedButRevertedBackToOriginalValuesButDateHasDifferentFormatAndSeparators = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("6af69bbd-47f0-4dc4-874a-4980752d4008"),
            addressUuid = UUID.fromString("8d41aacc-be93-40e1-a41e-ae4e192cc4d4"),
            name = "Anish",
            phoneNumber = "123456",
            gender = Female,
            colonyOrVillage = "Bathinda",
            district = "Hoshiarpur",
            state = "Bengaluru",
            dateOfBirthString = "1995-06-13"),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditGenderChanged(Transgender),
            PatientEditColonyOrVillageChanged("Batinda"),
            PatientEditDistrictTextChanged("Hosiarpur"),
            PatientEditStateTextChanged("Bangalore"),
            PatientEditDateOfBirthTextChanged("13/06/1996"),
            PatientEditPatientNameTextChanged("Anish"),
            PatientEditPhoneNumberTextChanged("123456"),
            PatientEditGenderChanged(Female),
            PatientEditColonyOrVillageChanged("Bathinda"),
            PatientEditDistrictTextChanged("Hoshiarpur"),
            PatientEditStateTextChanged("Bengaluru"),
            PatientEditDateOfBirthTextChanged("13/06/1995")),
        shouldShowConfirmDiscardChangesPopup = false)

    val nameChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("745579dd-b2f8-4841-bd66-09eae14d8e20"),
            addressUuid = UUID.fromString("7aae0e39-e9a7-494f-8191-e5a3ca15cb52"),
            name = "Anish"),
        inputEvents = listOf(PatientEditPatientNameTextChanged("Anisha")),
        shouldShowConfirmDiscardChangesPopup = true)

    val nameChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("bc91fdce-3897-4ac4-9c60-fa39e221a5ff"),
            addressUuid = UUID.fromString("f138e034-fc34-425c-9d13-3e5a8cd381bc"),
            name = "Anish"),
        inputEvents = listOf(
            PatientEditPatientNameTextChanged("Anisha"),
            PatientEditPatientNameTextChanged("Anish")),
        shouldShowConfirmDiscardChangesPopup = false)

    val nameChangeEventDispatchedWithTheSameValue = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("24650bc5-0cd1-4c50-8e0d-7ce1969a94f6"),
            addressUuid = UUID.fromString("b2e88f30-4512-476c-a950-39c54e9e68bb"),
            name = "Anish"),
        inputEvents = listOf(PatientEditPatientNameTextChanged("Anish")),
        shouldShowConfirmDiscardChangesPopup = false)

    val phoneNumberAdded = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("d39a4471-73bc-4b1d-9eaf-74260258e919"),
            addressUuid = UUID.fromString("b4d6d17f-26af-4c30-898f-490b3b2b63f2"),
            phoneNumber = null),
        inputEvents = listOf(PatientEditPhoneNumberTextChanged("12345")),
        shouldShowConfirmDiscardChangesPopup = true)

    val phoneNumberAddedAndRemoved = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("ed0400d7-9968-47f9-8227-371d0f8d26f6"),
            addressUuid = UUID.fromString("c3d74754-c9b0-4cb3-80e8-d86f6a6b50d4"),
            phoneNumber = null),
        inputEvents = listOf(
            PatientEditPhoneNumberTextChanged("12345"),
            PatientEditPhoneNumberTextChanged("")),
        shouldShowConfirmDiscardChangesPopup = false)

    val noPhoneNumberAndEmptyPhoneNumberChangedEvent = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("664f1b00-b33b-45ec-af6e-79d08806283e"),
            addressUuid = UUID.fromString("06282465-eaeb-45ab-949d-2ec2d6199a2e"),
            phoneNumber = null),
        inputEvents = listOf(PatientEditPhoneNumberTextChanged("")),
        shouldShowConfirmDiscardChangesPopup = false)

    val phoneNumberChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("ea27df22-9b81-4f14-8946-fbacce42bbf5"),
            addressUuid = UUID.fromString("e6a156c9-365d-43c4-8ed2-03fe8df4c17b"),
            phoneNumber = "1234567"),
        inputEvents = listOf(PatientEditPhoneNumberTextChanged("12345")),
        shouldShowConfirmDiscardChangesPopup = true)

    val phoneNumberChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("e184c407-bf6a-4757-9e15-78dac12884e8"),
            addressUuid = UUID.fromString("0705a463-bfb4-4a1a-a21b-107cef1b276b"),
            phoneNumber = "1234567"),
        inputEvents = listOf(
            PatientEditPhoneNumberTextChanged("123456"),
            PatientEditPhoneNumberTextChanged("1234567")),
        shouldShowConfirmDiscardChangesPopup = false)

    val colonyOrVillageChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("049727ba-5d75-4abd-9a16-1a7831fa9cfa"),
            addressUuid = UUID.fromString("989d0bd3-8b5f-4ba6-8561-a557405c42b3"),
            colonyOrVillage = "Batinda"),
        inputEvents = listOf(PatientEditColonyOrVillageChanged("Bathinda")),
        shouldShowConfirmDiscardChangesPopup = true)

    val colonyOrVillageChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("81f497de-4cf8-4cae-afd6-ea356946f6f9"),
            addressUuid = UUID.fromString("d56ebdb7-8325-402f-86f2-3c7a675d48dc"),
            colonyOrVillage = "Batinda"),
        inputEvents = listOf(
            PatientEditColonyOrVillageChanged("Bathinda"),
            PatientEditColonyOrVillageChanged("Batinda")),
        shouldShowConfirmDiscardChangesPopup = false)

    val colonyOrVillageChangeEventDispatchedWithTheSameValue = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("6829987a-ffad-4e64-99a6-e2ecbdb83609"),
            addressUuid = UUID.fromString("3f00dc36-ee0a-44e0-97a7-ddf7de3615bb"),
            colonyOrVillage = "Bathinda"),
        inputEvents = listOf(PatientEditColonyOrVillageChanged("Bathinda")),
        shouldShowConfirmDiscardChangesPopup = false)

    val districtChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("ff29c615-4b7d-4ba1-ae7a-55c246e06fb0"),
            addressUuid = UUID.fromString("14cd23e6-0d7f-4163-b487-e9e1947ed4a9"),
            district = "Hosiarpur"),
        inputEvents = listOf(PatientEditDistrictTextChanged("Hoshiarpur")),
        shouldShowConfirmDiscardChangesPopup = true)

    val districtChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("3e1f6170-7430-4256-8dff-e93ed184df4f"),
            addressUuid = UUID.fromString("ffcd0277-7f98-4658-b862-28f4ae3e92c7"),
            district = "Hosiarpur"),
        inputEvents = listOf(
            PatientEditDistrictTextChanged("Hoshiarpur"),
            PatientEditDistrictTextChanged("Hosiarpur")),
        shouldShowConfirmDiscardChangesPopup = false)

    val districtChangedEventDispatchedWithTheSameValue = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("424c84e8-559b-459c-9e64-3fd458a2b5b5"),
            addressUuid = UUID.fromString("c78e38b8-4bcb-4caa-9313-b971cae23c52"),
            district = "Hoshiarpur"),
        inputEvents = listOf(PatientEditDistrictTextChanged("Hoshiarpur")),
        shouldShowConfirmDiscardChangesPopup = false)

    val stateChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("3ee202bf-3985-4b0c-9281-b448bfbbc05e"),
            addressUuid = UUID.fromString("dff73ef8-0620-4f42-a7ed-5149ed5f0188"),
            state = "Bengaluru"
        ),
        inputEvents = listOf(PatientEditStateTextChanged("Bangalore")),
        shouldShowConfirmDiscardChangesPopup = true)

    val stateChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("a81f8ebd-38e1-4eed-bbcf-6d2f9245880c"),
            addressUuid = UUID.fromString("686413b9-431b-48af-952e-1f07821c9408"),
            state = "Bengaluru"
        ),
        inputEvents = listOf(
            PatientEditStateTextChanged("Bangalore"),
            PatientEditStateTextChanged("Bengaluru")),
        shouldShowConfirmDiscardChangesPopup = false)

    val stageChangedEventDispatchedWithTheSameValue = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("ed581f52-3648-412f-b030-d9c7e1ae1aae"),
            addressUuid = UUID.fromString("7d384a11-7e90-42a3-a45e-d63c2298f293"),
            state = "Bengaluru"
        ),
        inputEvents = listOf(PatientEditStateTextChanged("Bengaluru")),
        shouldShowConfirmDiscardChangesPopup = false)

    val genderChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("bd3ace1b-8516-4aec-8510-a59fe474b489"),
            addressUuid = UUID.fromString("00523312-bede-4313-bc46-00f84fff0b4a"),
            gender = Male
        ),
        inputEvents = listOf(PatientEditGenderChanged(Female)),
        shouldShowConfirmDiscardChangesPopup = true)

    val genderChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("f218df49-5fbf-4680-a438-6b10a84d629d"),
            addressUuid = UUID.fromString("a9dde750-a944-41b0-8e3e-d9157b7424c1"),
            gender = Male
        ),
        inputEvents = listOf(
            PatientEditGenderChanged(Female),
            PatientEditGenderChanged(Male)),
        shouldShowConfirmDiscardChangesPopup = false)

    val genderChangedEventDispatchedWithTheSameValue = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("1ff4de1b-189d-43e7-8c0d-5aa556fd9e38"),
            addressUuid = UUID.fromString("8b5e6a68-47a7-4446-90ce-2c5a9da45b3b"),
            gender = Male
        ),
        inputEvents = listOf(PatientEditGenderChanged(Male)),
        shouldShowConfirmDiscardChangesPopup = false)

    val ageChangedEventDispatchedWithTheSameValue = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("693f4b61-9462-4b2e-afc1-17eb5af9f175"),
            addressUuid = UUID.fromString("d7ebc0a5-4b71-42ef-a47f-b8d470e74b12"),
            ageValue = 30
        ),
        inputEvents = listOf(PatientEditAgeTextChanged("30")),
        shouldShowConfirmDiscardChangesPopup = false)

    val ageChangedAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("998fe915-99c1-4dd4-9dc3-46ce5977a40f"),
            addressUuid = UUID.fromString("6d2b3330-6dd9-4de5-a6f5-e2929ae48120"),
            ageValue = 30
        ),
        inputEvents = listOf(
            PatientEditAgeTextChanged("31"),
            PatientEditAgeTextChanged("30")),
        shouldShowConfirmDiscardChangesPopup = false)

    val ageChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("6a1c3a8c-1f46-4193-b0e0-19ea0d03cb7b"),
            addressUuid = UUID.fromString("2dd961f9-0443-4237-bda0-0ce5262c63a3"),
            ageValue = 30
        ),
        inputEvents = listOf(PatientEditAgeTextChanged("31")),
        shouldShowConfirmDiscardChangesPopup = true)

    val ageChangedToDateOfBirth = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("c03d3eb8-ef8f-4372-ae77-d10a77fb6d3a"),
            addressUuid = UUID.fromString("c39b2f8e-c38a-4e34-adec-d0cdafde1250"),
            ageValue = 30
        ),
        inputEvents = listOf(
            PatientEditAgeTextChanged(""),
            PatientEditDateOfBirthTextChanged("13/06/1995")),
        shouldShowConfirmDiscardChangesPopup = true)

    val ageChangedToDateOfBirthAndReverted = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("eddc456e-f27e-4f7e-bdbe-eb994db740b2"),
            addressUuid = UUID.fromString("73465488-3d61-4c22-89f6-8da498f0dba8"),
            ageValue = 30
        ),
        inputEvents = listOf(
            PatientEditAgeTextChanged(""),
            PatientEditDateOfBirthTextChanged("13/06/1995"),
            PatientEditDateOfBirthTextChanged(""),
            PatientEditAgeTextChanged("30")),
        shouldShowConfirmDiscardChangesPopup = false)

    val dateOfBirthFormatAndSeparatorsChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("50fc5672-5eca-4fc8-9639-450ab37bb1be"),
            addressUuid = UUID.fromString("97104af5-4a04-4341-b249-9a6186aead8b"),
            dateOfBirthString = "1995-06-13"
        ),
        inputEvents = listOf(PatientEditDateOfBirthTextChanged("13/06/1995")),
        shouldShowConfirmDiscardChangesPopup = false)

    val dateOfBirthValueChangedThenRevertedBackWithDifferentSeparatorsAndFormat = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("e7049567-c518-45aa-ad9f-542a3d9b0d8a"),
            addressUuid = UUID.fromString("0489852b-22c5-4b6f-86d6-dce901cd0c3c"),
            dateOfBirthString = "1995-06-13"
        ),
        inputEvents = listOf(
            PatientEditDateOfBirthTextChanged("13/06/1996"),
            PatientEditDateOfBirthTextChanged("13/06/1995")),
        shouldShowConfirmDiscardChangesPopup = false)

    val dateOfBirthYearFormatAndSeparatorsChanged = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("55ddb042-486f-4262-be69-0c66bf80d949"),
            addressUuid = UUID.fromString("9fceab06-4e10-47d8-aa8a-a4a6f02d01de"),
            dateOfBirthString = "1995-06-13"
        ),
        inputEvents = listOf(PatientEditDateOfBirthTextChanged("13/06/1996")),
        shouldShowConfirmDiscardChangesPopup = true)

    val dateOfBirthChangedToAge = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("f4772448-41e0-4d26-8cd7-1aa6ec1e336c"),
            addressUuid = UUID.fromString("38040581-ba03-4391-ae8d-bffe05fc98db"),
            dateOfBirthString = "1995-06-13"
        ),
        inputEvents = listOf(
            PatientEditDateOfBirthTextChanged(""),
            PatientEditAgeTextChanged("30")),
        shouldShowConfirmDiscardChangesPopup = true)

    val dateOfBirthChangedToAgeAndRevertedBackWithDifferentFormat = createConfirmDiscardChangesTestData(
        patientProfile = generatePatientProfile(
            patientUuid = UUID.fromString("4456b23f-ef05-413e-88e9-81acead06121"),
            addressUuid = UUID.fromString("6953267f-23d0-4ec7-9623-0dbd6c264189"),
            dateOfBirthString = "1995-06-13"
        ),
        inputEvents = listOf(
            PatientEditDateOfBirthTextChanged(""),
            PatientEditAgeTextChanged("30"),
            PatientEditAgeTextChanged(""),
            PatientEditDateOfBirthTextChanged("13/06/1995")),
        shouldShowConfirmDiscardChangesPopup = false)

    return listOf(
        noUserInputOnScreen,
        allFieldsChanged,
        allFieldsChangedAndAgeConvertedToDateOfBirth,
        allFieldsChangedAndDateOfBirthSeparatorsChanged,
        allFieldsChangedAndDateOfBirthConvertedToAge,
        allFieldsEditedButRevertedBackToOriginalValues,
        allFieldsEditedButRevertedBackToOriginalValuesButDateHasDifferentFormatAndSeparators,

        nameChanged,
        nameChangedAndReverted,
        nameChangeEventDispatchedWithTheSameValue,

        phoneNumberAdded,
        phoneNumberAddedAndRemoved,
        noPhoneNumberAndEmptyPhoneNumberChangedEvent,
        phoneNumberChanged,
        phoneNumberChangedAndReverted,

        colonyOrVillageChanged,
        colonyOrVillageChangedAndReverted,
        colonyOrVillageChangeEventDispatchedWithTheSameValue,

        districtChanged,
        districtChangedAndReverted,
        districtChangedEventDispatchedWithTheSameValue,

        stateChanged,
        stateChangedAndReverted,
        stageChangedEventDispatchedWithTheSameValue,

        genderChanged,
        genderChangedAndReverted,
        genderChangedEventDispatchedWithTheSameValue,

        ageChanged,
        ageChangedAndReverted,
        ageChangedEventDispatchedWithTheSameValue,
        ageChangedToDateOfBirth,
        ageChangedToDateOfBirthAndReverted,

        dateOfBirthFormatAndSeparatorsChanged,
        dateOfBirthValueChangedThenRevertedBackWithDifferentSeparatorsAndFormat,
        dateOfBirthYearFormatAndSeparatorsChanged,
        dateOfBirthChangedToAge,
        dateOfBirthChangedToAgeAndRevertedBackWithDifferentFormat
    )
  }

  private fun createConfirmDiscardChangesTestData(
      patientProfile: PatientProfile,
      inputEvents: List<UiEvent>,
      shouldShowConfirmDiscardChangesPopup: Boolean
  ): ConfirmDiscardChangesTestParams {
    val preCreateInputEvents = listOf(
        PatientEditPatientNameTextChanged(patientProfile.patient.fullName),
        PatientEditDistrictTextChanged(patientProfile.address.district),
        PatientEditColonyOrVillageChanged(patientProfile.address.colonyOrVillage ?: ""),
        PatientEditStateTextChanged(patientProfile.address.state),
        PatientEditGenderChanged(patientProfile.patient.gender),
        PatientEditPhoneNumberTextChanged(patientProfile.phoneNumbers.firstOrNull()?.number ?: "")
    ) + patientProfile.let { (patient, _, _) ->
      if (patient.age != null) {
        listOf(PatientEditAgeTextChanged(patient.age!!.value.toString()))
      } else {
        listOf(PatientEditDateOfBirthTextChanged(patient.dateOfBirth!!.format(dateOfBirthFormat)))
      }
    }

    return ConfirmDiscardChangesTestParams(
        patientProfile.patient,
        patientProfile.address,
        if (patientProfile.phoneNumbers.isEmpty()) null else patientProfile.phoneNumbers.first(),
        preCreateInputEvents + inputEvents,
        shouldShowConfirmDiscardChangesPopup
    )
  }

  private fun generatePatientProfile(
      patientUuid: UUID,
      addressUuid: UUID,
      name: String? = null,
      phoneNumber: String? = null,
      gender: Gender? = null,
      ageValue: Int? = null,
      dateOfBirthString: String? = null,
      colonyOrVillage: String? = null,
      district: String? = null,
      state: String? = null
  ): PatientProfile {
    return PatientProfile(
        patient = PatientMocker.patient(uuid = patientUuid, addressUuid = addressUuid),
        address = PatientMocker.address(uuid = addressUuid),
        phoneNumbers = phoneNumber?.let { listOf(PatientMocker.phoneNumber(patientUuid = patientUuid, number = it)) }
            ?: emptyList(),
        businessIds = emptyList()
    ).let { profile ->
      if (gender != null) {
        return@let profile.copy(patient = profile.patient.copy(gender = gender))
      }
      profile

    }.let { profile ->
      if (name != null) {
        return@let profile.copy(patient = profile.patient.copy(fullName = name))
      }
      profile

    }.let { profile ->
      if (colonyOrVillage != null) {
        return@let profile.copy(address = profile.address.copy(colonyOrVillage = colonyOrVillage))
      }
      profile

    }.let { profile ->
      if (district != null) {
        return@let profile.copy(address = profile.address.copy(district = district))
      }
      profile

    }.let { profile ->
      if (state != null) {
        return@let profile.copy(address = profile.address.copy(state = state))
      }
      profile

    }.let { profile ->
      if (ageValue != null) {
        val age = Age(ageValue, Instant.now(utcClock))
        return@let profile.copy(patient = profile.patient.copy(age = age, dateOfBirth = null))

      } else if (dateOfBirthString != null) {
        val dateOfBirth = LocalDate.parse(dateOfBirthString)
        return@let profile.copy(patient = profile.patient.copy(age = null, dateOfBirth = dateOfBirth))
      }
      profile
    }
  }
}

data class DateOfBirthTestParams(
    val dateOfBirth: String,
    val dobValidationResult: UserInputDateValidator.Result,
    val expectedError: PatientEditValidationError
)

data class ConfirmDiscardChangesTestParams(
    val existingSavedPatient: Patient,
    val existingSavedAddress: PatientAddress,
    val existingSavedPhoneNumber: PatientPhoneNumber?,
    val inputEvents: List<UiEvent>,
    val shouldShowConfirmDiscardChangesPopup: Boolean
)

data class HidingErrorsOnTextChangeParams(
    val inputChange: UiEvent,
    val expectedErrorsToHide: Set<PatientEditValidationError>
)
