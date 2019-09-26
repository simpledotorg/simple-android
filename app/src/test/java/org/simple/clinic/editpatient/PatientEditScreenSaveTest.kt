package org.simple.clinic.editpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
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
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.IndianPhoneNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenSaveTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private lateinit var screen: PatientEditScreen
  private lateinit var patientRepository: PatientRepository
  private lateinit var errorConsumer: (Throwable) -> Unit

  private val utcClock: TestUtcClock = TestUtcClock()
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val nextYear = LocalDate.now().year + 1

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
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
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
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
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
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
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
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
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
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditAgeTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT))
  }

  @Test
  @Parameters(method = "params for date of birth should be validated")
  fun `when save is clicked, the date of birth should be validated`(
      dateOfBirthTestParams: DateOfBirthTestParams
  ) {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))
    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditDateOfBirthTextChanged(dateOfBirthTestParams.dateOfBirth))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(dateOfBirthTestParams.expectedError))
  }

  @Suppress("Unused")
  private fun `params for date of birth should be validated`(): List<DateOfBirthTestParams> {
    return listOf(
        DateOfBirthTestParams("20/40-80", InvalidPattern, INVALID_DATE_OF_BIRTH),
        DateOfBirthTestParams("01/01/$nextYear", DateIsInFuture, DATE_OF_BIRTH_IN_FUTURE)
    )
  }

  data class DateOfBirthTestParams(
      val dateOfBirth: String,
      val dobValidationResult: UserInputDateValidator.Result,
      val expectedError: PatientEditValidationError
  )

  @Test
  @Parameters(method = "params for saving patient on save clicked")
  fun `when save is clicked, the patient details must be updated if there are no errors`(testParams: SavePatientTestParams) {
    val (existingSavedPatient,
        existingSavedAddress,
        existingSavedPhoneNumber,
        advanceClockBy,
        inputEvents,
        shouldSavePatient,
        expectedSavedPatient,
        expectedSavedPatientAddress,
        expectedSavedPatientPhoneNumber
    ) = testParams

    val patientUuid = existingSavedPatient.uuid

    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePhoneNumberForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())).thenReturn(Completable.complete())

    utcClock.advanceBy(advanceClockBy)
    uiEvents.onNext(PatientEditScreenCreated.from(existingSavedPatient, existingSavedAddress, existingSavedPhoneNumber))
    inputEvents.forEach { uiEvents.onNext(it) }
    uiEvents.onNext(PatientEditSaveClicked())

    if (!shouldSavePatient) {
      verify(patientRepository, never()).updatePatient(any())
      verify(patientRepository, never()).updateAddressForPatient(eq(patientUuid), any())
      verify(patientRepository, never()).updatePhoneNumberForPatient(eq(patientUuid), any())
      verify(patientRepository, never()).createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())
      verify(screen, never()).goBack()
      return
    }

    verify(patientRepository).updatePatient(expectedSavedPatient!!)
    verify(patientRepository).updateAddressForPatient(expectedSavedPatient.uuid, expectedSavedPatientAddress!!)

    if (expectedSavedPatientPhoneNumber != null) {
      if (existingSavedPhoneNumber == null) {
        verify(patientRepository).createPhoneNumberForPatient(
            patientUuid = expectedSavedPatientPhoneNumber.patientUuid,
            number = expectedSavedPatientPhoneNumber.number,
            phoneNumberType = PatientPhoneNumberType.Mobile,
            active = true
        )
      } else {
        verify(patientRepository).updatePhoneNumberForPatient(expectedSavedPatient.uuid, expectedSavedPatientPhoneNumber)
      }

    } else {
      verify(patientRepository, never()).createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())
      verify(patientRepository, never()).updatePhoneNumberForPatient(eq(patientUuid), any())
    }
    verify(screen).goBack()
  }

  @Suppress("Unused")
  private fun `params for saving patient on save clicked`(): List<SavePatientTestParams> {
    val oneYear = Duration.ofDays(365L)
    val twoYears = oneYear.plus(oneYear)

    return listOf(
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditColonyOrVillageChanged("Colony"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Male),
                PatientEditPhoneNumberTextChanged("12345678"),
                PatientEditDateOfBirthTextChanged("20/05/1985")),
            shouldSavePatient = true,
            createExpectedPatient = { it.copy(fullName = "Name", gender = Gender.Male, dateOfBirth = LocalDate.of(1985, Month.MAY, 20)) },
            createExpectedAddress = { it.copy(district = "District", colonyOrVillage = "Colony", state = "State") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "12345678") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "12345678")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            advanceClockBy = oneYear,
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditColonyOrVillageChanged("Colony"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Male),
                PatientEditPhoneNumberTextChanged("12345678"),
                PatientEditDateOfBirthTextChanged(""),
                PatientEditAgeTextChanged("22")),
            shouldSavePatient = true,
            createExpectedPatient = {
              val expectedAge = Age(22, Instant.now(utcClock).plus(oneYear))

              it.copy(fullName = "Name", gender = Gender.Male, dateOfBirth = null, age = expectedAge)
            },
            createExpectedAddress = { it.copy(district = "District", colonyOrVillage = "Colony", state = "State") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "12345678") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "12345678")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditColonyOrVillageChanged("Colony"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Male),
                PatientEditPhoneNumberTextChanged("")),
            shouldSavePatient = true,
            createExpectedPatient = { it.copy(fullName = "Name", gender = Gender.Male) },
            createExpectedAddress = { it.copy(district = "District", colonyOrVillage = "Colony", state = "State") }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditAgeTextChanged("25")),
            shouldSavePatient = true,
            createExpectedPatient = {
              val expectedAge = Age(25, Instant.now(utcClock))

              it.copy(fullName = "Name", gender = Gender.Transgender, age = expectedAge)
            },
            createExpectedAddress = { it.copy(district = "District", state = "State") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "123456") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "123456")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditAgeTextChanged(""),
                PatientEditDateOfBirthTextChanged("25/06/1965")),
            shouldSavePatient = true,
            createExpectedPatient = {
              it.copy(
                  fullName = "Name",
                  gender = Gender.Transgender,
                  age = null,
                  dateOfBirth = LocalDate.parse("1965-06-25"))
            },
            createExpectedAddress = { it.copy(district = "District", state = "State") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "123456") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "123456")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            advanceClockBy = twoYears,
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditAgeTextChanged("25")),
            shouldSavePatient = true,
            createExpectedPatient = {
              val expectedAge = Age(25, Instant.now(utcClock).plus(twoYears))

              it.copy(fullName = "Name", gender = Gender.Transgender, age = expectedAge)
            },
            createExpectedAddress = { it.copy(district = "District", state = "State") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "123456") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "123456")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567")),
            shouldSavePatient = true,
            createExpectedPatient = { it.copy(fullName = "Name 2", gender = Gender.Transgender) },
            createExpectedAddress = { it.copy(district = "District", state = "State 2") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "1234567") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "1234567")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditAgeTextChanged("")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditDateOfBirthTextChanged("12/34")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditDateOfBirthTextChanged("30/11/$nextYear")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditAgeTextChanged("")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditAgeTextChanged(""),
                PatientEditDateOfBirthTextChanged("30/11/$nextYear")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditAgeTextChanged(""),
                PatientEditDateOfBirthTextChanged("30/11")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("1234")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditPatientNameTextChanged("")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("12345678901234")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged(""),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender)),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged(""),
                PatientEditGenderChanged(Gender.Female)),
            shouldSavePatient = false)
    )
  }

  private fun createPatientProfile(shouldAddNumber: Boolean, shouldHaveAge: Boolean): PatientProfile {
    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val patient = if (shouldHaveAge) {
      PatientMocker.patient(
          uuid = patientUuid,
          age = Age(20, Instant.now(utcClock)),
          dateOfBirth = null,
          addressUuid = addressUuid)

    } else {
      PatientMocker.patient(
          uuid = patientUuid,
          age = null,
          dateOfBirth = LocalDate.now(utcClock),
          addressUuid = addressUuid
      )
    }

    return PatientProfile(
        patient = patient,
        address = PatientMocker.address(uuid = addressUuid),
        phoneNumbers = if (shouldAddNumber) listOf(PatientMocker.phoneNumber(patientUuid = patientUuid)) else emptyList(),
        businessIds = emptyList()
    )
  }

  private fun createSavePatientTestParams(
      patientProfile: PatientProfile,
      advanceClockBy: Duration = Duration.ZERO,
      inputEvents: List<UiEvent>,
      shouldSavePatient: Boolean,
      createExpectedPatient: (Patient) -> Patient = { it },
      createExpectedAddress: (PatientAddress) -> PatientAddress = { it },
      createExpectedPhoneNumber: (UUID, PatientPhoneNumber?) -> PatientPhoneNumber? = { _, phoneNumber -> phoneNumber }
  ): SavePatientTestParams {
    val expectedPatientPhoneNumber = if (shouldSavePatient) {
      val alreadySavedPhoneNumber = if (patientProfile.phoneNumbers.isEmpty()) null else patientProfile.phoneNumbers.first()
      createExpectedPhoneNumber(patientProfile.patient.uuid, alreadySavedPhoneNumber)
    } else {
      null
    }

    val preCreateInputEvents = listOf(
        PatientEditPatientNameTextChanged(patientProfile.patient.fullName),
        PatientEditDistrictTextChanged(patientProfile.address.district),
        PatientEditColonyOrVillageChanged(patientProfile.address.colonyOrVillage ?: ""),
        PatientEditStateTextChanged(patientProfile.address.state),
        PatientEditGenderChanged(patientProfile.patient.gender),
        PatientEditPhoneNumberTextChanged(patientProfile.phoneNumbers.firstOrNull()?.number ?: ""),

        if (patientProfile.patient.age != null) {
          PatientEditAgeTextChanged(patientProfile.patient.age!!.value.toString())
        } else {
          PatientEditDateOfBirthTextChanged(dateOfBirthFormat.format(patientProfile.patient.dateOfBirth!!))
        }
    )

    return SavePatientTestParams(
        patientProfile.patient,
        patientProfile.address,
        patientProfile.phoneNumbers.firstOrNull(),
        advanceClockBy,
        preCreateInputEvents + inputEvents,
        shouldSavePatient,
        if (shouldSavePatient) createExpectedPatient(patientProfile.patient) else null,
        if (shouldSavePatient) createExpectedAddress(patientProfile.address) else null,
        expectedPatientPhoneNumber
    )
  }

  data class SavePatientTestParams(
      val existingSavedPatient: Patient,
      val existingSavedAddress: PatientAddress,
      val existingSavedPhoneNumber: PatientPhoneNumber?,
      val advanceClockBy: Duration,
      val inputEvents: List<UiEvent>,
      val shouldSavePatient: Boolean,
      val expectedSavedPatient: Patient?,
      val expectedSavedPatientAddress: PatientAddress?,
      val expectedSavedPatientPhoneNumber: PatientPhoneNumber?
  )

  @Test
  @Parameters(method = "params for validating all fields on save clicks")
  fun `when save is clicked, all fields should be validated`(validateFieldsTestParams: ValidateFieldsTestParams) {
    val (alreadyPresentPhoneNumber,
        name,
        colonyOrVillage,
        district,
        state,
        age,
        dateOfBirth,
        expectedErrors,
        enteredPhoneNumber
    ) = validateFieldsTestParams

    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val patientUuid = patient.uuid
    val phoneNumber = alreadyPresentPhoneNumber?.copy(patientUuid = patientUuid)

    whenever(patientRepository.createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePhoneNumberForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))

    uiEvents.onNext(PatientEditPatientNameTextChanged(name))
    uiEvents.onNext(PatientEditPhoneNumberTextChanged(enteredPhoneNumber))
    uiEvents.onNext(PatientEditColonyOrVillageChanged(colonyOrVillage))
    uiEvents.onNext(PatientEditDistrictTextChanged(district))
    uiEvents.onNext(PatientEditStateTextChanged(state))
    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))

    if (age != null) {
      uiEvents.onNext(PatientEditDateOfBirthTextChanged(""))
      uiEvents.onNext(PatientEditAgeTextChanged(age))
    }

    if (dateOfBirth != null) {
      uiEvents.onNext(PatientEditAgeTextChanged(""))
      uiEvents.onNext(PatientEditDateOfBirthTextChanged(dateOfBirth))
    }

    if (age == null && dateOfBirth == null) {
      uiEvents.onNext(PatientEditAgeTextChanged(""))
    }

    uiEvents.onNext(PatientEditSaveClicked())

    if (expectedErrors.isNotEmpty()) {
      // This is order dependent because finding the first field
      // with error is only possible once the errors are set.
      val inOrder = inOrder(screen)

      inOrder.verify(screen).showValidationErrors(expectedErrors)
      inOrder.verify(screen).scrollToFirstFieldWithError()

    } else {
      verify(screen, never()).showValidationErrors(any())
      verify(screen, never()).scrollToFirstFieldWithError()
    }
  }

  @Suppress("Unused")
  private fun `params for validating all fields on save clicks`(): List<ValidateFieldsTestParams> {
    return listOf(
        ValidateFieldsTestParams(
            PatientMocker.phoneNumber(),
            "",
            "",
            "",
            "",
            "1",
            null,
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_EMPTY, COLONY_OR_VILLAGE_EMPTY, DISTRICT_EMPTY, STATE_EMPTY),
            ""
        ),
        ValidateFieldsTestParams(
            null,
            "",
            "",
            "",
            "",
            "",
            null,
            setOf(FULL_NAME_EMPTY, COLONY_OR_VILLAGE_EMPTY, DISTRICT_EMPTY, STATE_EMPTY, BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
            enteredPhoneNumber = "1234567890"
        ),
        ValidateFieldsTestParams(
            PatientMocker.phoneNumber(),
            "",
            "Colony",
            "",
            "",
            "1",
            null,
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, DISTRICT_EMPTY, STATE_EMPTY),
            enteredPhoneNumber = "1234"
        ),
        ValidateFieldsTestParams(
            null,
            "",
            "Colony",
            "",
            "",
            "",
            null,
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, DISTRICT_EMPTY, STATE_EMPTY, BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
            enteredPhoneNumber = "1234"
        ),
        ValidateFieldsTestParams(
            PatientMocker.phoneNumber(),
            "Name",
            "",
            "District",
            "",
            "1",
            null,
            setOf(PHONE_NUMBER_LENGTH_TOO_LONG, COLONY_OR_VILLAGE_EMPTY, STATE_EMPTY),
            "12345678901234"
        ),
        ValidateFieldsTestParams(
            null,
            "Name",
            "",
            "District",
            "",
            null,
            "24/24/2000",
            setOf(PHONE_NUMBER_LENGTH_TOO_LONG, COLONY_OR_VILLAGE_EMPTY, STATE_EMPTY, INVALID_DATE_OF_BIRTH),
            "12345678901234"
        ),
        ValidateFieldsTestParams(
            PatientMocker.phoneNumber(),
            "",
            "Colony",
            "District",
            "",
            null,
            null,
            setOf(FULL_NAME_EMPTY, STATE_EMPTY, BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
            "1234567890"
        ),
        ValidateFieldsTestParams(
            null,
            "",
            "Colony",
            "District",
            "",
            null,
            "01/01/$nextYear",
            setOf(FULL_NAME_EMPTY, STATE_EMPTY, DATE_OF_BIRTH_IN_FUTURE),
            "1234567890"
        ),
        ValidateFieldsTestParams(
            null,
            "",
            "Colony",
            "District",
            "State",
            "",
            null,
            setOf(FULL_NAME_EMPTY, BOTH_DATEOFBIRTH_AND_AGE_ABSENT),
            "12334567890"
        ),
        ValidateFieldsTestParams(
            PatientMocker.phoneNumber(),
            "Name",
            "Colony",
            "District",
            "State",
            "1",
            null,
            emptySet(),
            "1234567890"
        ),
        ValidateFieldsTestParams(
            null,
            "Name",
            "Colony",
            "District",
            "State",
            null,
            "01/01/2000",
            emptySet(),
            "1234567890"
        )
    )
  }

  data class ValidateFieldsTestParams(
      val alreadyPresentPhoneNumber: PatientPhoneNumber?,
      val name: String,
      val colonyOrVillage: String,
      val district: String,
      val state: String,
      val age: String?,
      val dateOfBirth: String?,
      val expectedErrors: Set<PatientEditValidationError>,
      val enteredPhoneNumber: String = ""
  )

  @Test
  @Parameters(method = "params for validating phone numbers")
  fun `when save is clicked, invalid phone numbers should show appropriate errors`(testParams: ValidatePhoneNumberTestParams) {
    val (alreadyPresentPhoneNumber,
        enteredPhoneNumber,
        expectedError
    ) = testParams

    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val patientUuid = patient.uuid

    whenever(patientRepository.createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, alreadyPresentPhoneNumber))

    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(enteredPhoneNumber))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(expectedError))
  }

  @Suppress("Unused")
  private fun `params for validating phone numbers`(): List<ValidatePhoneNumberTestParams> {
    return listOf(
        ValidatePhoneNumberTestParams(null, "1234", PHONE_NUMBER_LENGTH_TOO_SHORT),
        ValidatePhoneNumberTestParams(null, "12345678901234", PHONE_NUMBER_LENGTH_TOO_LONG),
        ValidatePhoneNumberTestParams(PatientMocker.phoneNumber(), "12345678901234", PHONE_NUMBER_LENGTH_TOO_LONG),
        ValidatePhoneNumberTestParams(PatientMocker.phoneNumber(), "", PHONE_NUMBER_EMPTY),
        ValidatePhoneNumberTestParams(PatientMocker.phoneNumber(), "1234", PHONE_NUMBER_LENGTH_TOO_SHORT)
    )
  }

  data class ValidatePhoneNumberTestParams(
      val alreadyPresentPhoneNumber: PatientPhoneNumber?,
      val enteredPhoneNumber: String,
      val expectedError: PatientEditValidationError
  )

  @Test
  fun `when save is clicked and phone number is not already saved, entering a blank phone number should not show errors`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()

    whenever(patientRepository.updateAddressForPatient(eq(patient.uuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, null))

    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen, never()).showValidationErrors(any())
  }
}
