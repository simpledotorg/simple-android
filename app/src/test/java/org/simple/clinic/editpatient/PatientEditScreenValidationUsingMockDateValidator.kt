package org.simple.clinic.editpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
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
import org.simple.clinic.editpatient.PatientEditValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.PatientEditValidationError.INVALID_DATE_OF_BIRTH
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
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenValidationUsingMockDateValidator {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  val utcClock: TestUtcClock = TestUtcClock()

  private lateinit var screen: PatientEditScreen
  private lateinit var patientRepository: PatientRepository
  private val numberValidator: PhoneNumberValidator = IndianPhoneNumberValidator()
  private lateinit var controller: PatientEditScreenController

  private lateinit var errorConsumer: (Throwable) -> Unit
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private lateinit var dobValidator: UserInputDateValidator

  @Before
  fun setUp() {
    screen = mock()
    patientRepository = mock()
    dobValidator = mock()

    whenever(dobValidator.dateInUserTimeZone()).thenReturn(LocalDate.now(utcClock))

    controller = PatientEditScreenController(
        patientRepository,
        numberValidator,
        utcClock,
        TestUserClock(),
        dobValidator,
        dateOfBirthFormat)

    errorConsumer = { throw it }

    uiEvents
        .compose(controller)
        .subscribe({ uiChange -> uiChange(screen) }, { e -> errorConsumer(e) })
  }

  @Test
  @Parameters(method = "params for date of birth should be validated")
  fun `when save is clicked, the date of birth should be validated`(
      dateOfBirthTestParams: DateOfBirthTestParams
  ) {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(dobValidator.validate(any(), any())).thenReturn(dateOfBirthTestParams.dobValidationResult)

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
        DateOfBirthTestParams("01/01/2000", InvalidPattern, INVALID_DATE_OF_BIRTH),
        DateOfBirthTestParams("01/01/2000", DateIsInFuture, DATE_OF_BIRTH_IN_FUTURE)
    )
  }

  @Test
  @Parameters(method = "params for saving patient on save clicked")
  fun `when save is clicked, the patient details must be updated if there are no errors`(
      existingSavedPatient: Patient,
      existingSavedAddress: PatientAddress,
      existingSavedPhoneNumber: PatientPhoneNumber?,
      numberValidationResult: PhoneNumberValidator.Result,
      userInputDateOfBirthValidationResult: UserInputDateValidator.Result,
      advanceClockBy: Duration,
      inputEvents: List<UiEvent>,
      shouldSavePatient: Boolean,
      expectedSavedPatient: Patient?,
      expectedSavedPatientAddress: PatientAddress?,
      expectedSavedPatientPhoneNumber: PatientPhoneNumber?
  ) {
    val patientUuid = existingSavedPatient.uuid

    whenever(dobValidator.validate(any(), any())).thenReturn(userInputDateOfBirthValidationResult)

    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePhoneNumberForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())).thenReturn(Completable.complete())

    utcClock.advanceBy(advanceClockBy)
    uiEvents.onNext(PatientEditScreenCreated.from(existingSavedPatient, existingSavedAddress, existingSavedPhoneNumber))
    inputEvents.forEach { uiEvents.onNext(it) }
    uiEvents.onNext(PatientEditSaveClicked())

    if (shouldSavePatient) {
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

    } else {
      verify(patientRepository, never()).updatePatient(any())
      verify(patientRepository, never()).updateAddressForPatient(eq(patientUuid), any())
      verify(patientRepository, never()).updatePhoneNumberForPatient(eq(patientUuid), any())
      verify(patientRepository, never()).createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())
      verify(screen, never()).goBack()
    }
  }

  @Suppress("Unused")
  private fun `params for saving patient on save clicked`(): List<List<Any?>> {
    val oneYear = Duration.ofDays(365L)
    val twoYears = oneYear.plus(oneYear)

    return listOf(
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            userInputDateOfBirthValidationResult = Valid(LocalDate.of(1985, Month.MAY, 20)),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            userInputDateOfBirthValidationResult = Valid(LocalDate.parse("1949-01-01")),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = false, shouldHaveAge = false),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            userInputDateOfBirthValidationResult = Valid(LocalDate.parse("1945-01-01")),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            userInputDateOfBirthValidationResult = Valid(LocalDate.parse("1965-06-25")),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            userInputDateOfBirthValidationResult = Valid(LocalDate.parse("1947-01-01")),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = true),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            userInputDateOfBirthValidationResult = InvalidPattern,
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            userInputDateOfBirthValidationResult = DateIsInFuture,
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name 1"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State 1"),
                PatientEditGenderChanged(Gender.Transgender),
                PatientEditPhoneNumberTextChanged("123456"),
                PatientEditStateTextChanged("State 2"),
                PatientEditPatientNameTextChanged("Name 2"),
                PatientEditPhoneNumberTextChanged("1234567"),
                PatientEditDateOfBirthTextChanged("30/11/2000")),
            shouldSavePatient = false),
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            userInputDateOfBirthValidationResult = DateIsInFuture,
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
                PatientEditDateOfBirthTextChanged("30/11/2000")),
            shouldSavePatient = false),
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            userInputDateOfBirthValidationResult = InvalidPattern,
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            numberValidationResult = LENGTH_TOO_SHORT,
            userInputDateOfBirthValidationResult = InvalidPattern,
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender)),
            shouldSavePatient = false),
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = false, shouldHaveAge = false),
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
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            numberValidationResult = LENGTH_TOO_LONG,
            inputEvents = listOf(
                PatientEditPatientNameTextChanged(""),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender)),
            shouldSavePatient = false),
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged(""),
                PatientEditStateTextChanged("State"),
                PatientEditGenderChanged(Gender.Transgender)),
            shouldSavePatient = false),
        generateTestData(
            patientProfile = generatePatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            numberValidationResult = BLANK,
            inputEvents = listOf(
                PatientEditPatientNameTextChanged("Name"),
                PatientEditDistrictTextChanged("District"),
                PatientEditStateTextChanged(""),
                PatientEditGenderChanged(Gender.Female)),
            shouldSavePatient = false)
    )
  }

  private fun generatePatientProfile(shouldAddNumber: Boolean, shouldHaveAge: Boolean): PatientProfile {
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

  private fun generateTestData(
      patientProfile: PatientProfile,
      numberValidationResult: PhoneNumberValidator.Result = VALID,
      userInputDateOfBirthValidationResult: UserInputDateValidator.Result = Valid(LocalDate.parse("1947-01-01")),
      advanceClockBy: Duration = Duration.ZERO,
      inputEvents: List<UiEvent>,
      shouldSavePatient: Boolean,
      createExpectedPatient: (Patient) -> Patient = { it },
      createExpectedAddress: (PatientAddress) -> PatientAddress = { it },
      createExpectedPhoneNumber: (UUID, PatientPhoneNumber?) -> PatientPhoneNumber? = { _, phoneNumber -> phoneNumber }
  ): List<Any?> {
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

    return listOf(
        patientProfile.patient,
        patientProfile.address,
        patientProfile.phoneNumbers.firstOrNull(),
        numberValidationResult,
        userInputDateOfBirthValidationResult,
        advanceClockBy,
        preCreateInputEvents + inputEvents,
        shouldSavePatient,
        if (shouldSavePatient) createExpectedPatient(patientProfile.patient) else null,
        if (shouldSavePatient) createExpectedAddress(patientProfile.address) else null,
        expectedPatientPhoneNumber
    )
  }
}
