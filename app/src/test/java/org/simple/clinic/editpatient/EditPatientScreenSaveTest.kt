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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.editpatient.EditPatientValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.EditPatientValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.EditPatientValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.EditPatientValidationError.STATE_EMPTY
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
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.mobius.migration.MobiusTestFixture
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class EditPatientScreenSaveTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<EditPatientEvent>()
  private val ui: EditPatientUi = mock()
  private val viewRenderer = EditPatientViewRenderer(ui)
  private val patientRepository: PatientRepository = mock()

  private val utcClock: TestUtcClock = TestUtcClock()
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val nextYear = LocalDate.now().year + 1

  @Test
  fun `when save is clicked, patient name should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(NameChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(FULL_NAME_EMPTY))
  }

  @Test
  fun `when save is clicked, the colony should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(ColonyOrVillageChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(COLONY_OR_VILLAGE_EMPTY))
  }

  @Test
  fun `when save is clicked, the district should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(DistrictChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(DISTRICT_EMPTY))
  }

  @Test
  fun `when save is clicked, the state should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(StateChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(STATE_EMPTY))
  }

  @Test
  fun `when save is clicked, the age should be validated`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    screenCreated(patient, address, phoneNumber)
    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(AgeChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT))
  }

  @Test
  @Parameters(method = "params for date of birth should be validated")
  fun `when save is clicked, the date of birth should be validated`(
      dateOfBirthTestParams: DateOfBirthTestParams
  ) {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber: PatientPhoneNumber? = null

    screenCreated(patient, address, phoneNumber)
    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(DateOfBirthChanged(dateOfBirthTestParams.dateOfBirth))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(dateOfBirthTestParams.expectedError))
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
      val expectedError: EditPatientValidationError
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
    screenCreated(existingSavedPatient, existingSavedAddress, existingSavedPhoneNumber)
    inputEvents.forEach { uiEvents.onNext(it) }
    uiEvents.onNext(SaveClicked)

    if (!shouldSavePatient) {
      verify(patientRepository, never()).updatePatient(any())
      verify(patientRepository, never()).updateAddressForPatient(eq(patientUuid), any())
      verify(patientRepository, never()).updatePhoneNumberForPatient(eq(patientUuid), any())
      verify(patientRepository, never()).createPhoneNumberForPatient(eq(patientUuid), any(), any(), any())
      verify(ui, never()).goBack()
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
    verify(ui).goBack()
  }

  @Suppress("Unused")
  private fun `params for saving patient on save clicked`(): List<SavePatientTestParams> {
    val oneYear = Duration.ofDays(365L)
    val twoYears = oneYear.plus(oneYear)

    return listOf(
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name"),
                DistrictChanged("District"),
                ColonyOrVillageChanged("Colony"),
                StateChanged("State"),
                GenderChanged(Gender.Male),
                PhoneNumberChanged("12345678"),
                DateOfBirthChanged("20/05/1985")),
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
                NameChanged("Name"),
                DistrictChanged("District"),
                ColonyOrVillageChanged("Colony"),
                StateChanged("State"),
                GenderChanged(Gender.Male),
                PhoneNumberChanged("12345678"),
                DateOfBirthChanged(""),
                AgeChanged("22")),
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
                NameChanged("Name"),
                DistrictChanged("District"),
                ColonyOrVillageChanged("Colony"),
                StateChanged("State"),
                GenderChanged(Gender.Male),
                PhoneNumberChanged("")),
            shouldSavePatient = true,
            createExpectedPatient = { it.copy(fullName = "Name", gender = Gender.Male) },
            createExpectedAddress = { it.copy(district = "District", colonyOrVillage = "Colony", state = "State") }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                NameChanged("Name"),
                DistrictChanged("District"),
                StateChanged("State"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                AgeChanged("25")),
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
                NameChanged("Name"),
                DistrictChanged("District"),
                StateChanged("State"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                AgeChanged(""),
                DateOfBirthChanged("25/06/1965")),
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
                NameChanged("Name"),
                DistrictChanged("District"),
                StateChanged("State"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                AgeChanged("25")),
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
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567")),
            shouldSavePatient = true,
            createExpectedPatient = { it.copy(fullName = "Name 2", gender = Gender.Transgender) },
            createExpectedAddress = { it.copy(district = "District", state = "State 2") },
            createExpectedPhoneNumber = { patientId, alreadyPresentPhoneNumber ->
              alreadyPresentPhoneNumber?.copy(number = "1234567") ?: PatientMocker.phoneNumber(patientUuid = patientId, number = "1234567")
            }),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                AgeChanged("")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                DateOfBirthChanged("12/34")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                DateOfBirthChanged("30/11/$nextYear")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                AgeChanged("")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                AgeChanged(""),
                DateOfBirthChanged("30/11/$nextYear")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = true),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                AgeChanged(""),
                DateOfBirthChanged("30/11")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name"),
                DistrictChanged("District"),
                StateChanged("State"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("1234")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name 1"),
                DistrictChanged("District"),
                StateChanged("State 1"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("123456"),
                StateChanged("State 2"),
                NameChanged("Name 2"),
                PhoneNumberChanged("1234567"),
                NameChanged("")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                DistrictChanged("District"),
                StateChanged("State"),
                GenderChanged(Gender.Transgender),
                PhoneNumberChanged("12345678901234")),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = true, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name"),
                DistrictChanged(""),
                StateChanged("State"),
                GenderChanged(Gender.Transgender)),
            shouldSavePatient = false),
        createSavePatientTestParams(
            patientProfile = createPatientProfile(shouldAddNumber = false, shouldHaveAge = false),
            inputEvents = listOf(
                NameChanged("Name"),
                DistrictChanged("District"),
                StateChanged(""),
                GenderChanged(Gender.Female)),
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
      inputEvents: List<EditPatientEvent>,
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
        NameChanged(patientProfile.patient.fullName),
        DistrictChanged(patientProfile.address.district),
        ColonyOrVillageChanged(patientProfile.address.colonyOrVillage
            ?: ""),
        StateChanged(patientProfile.address.state),
        GenderChanged(patientProfile.patient.gender),
        PhoneNumberChanged(patientProfile.phoneNumbers.firstOrNull()?.number
            ?: ""),

        if (patientProfile.patient.age != null) {
          AgeChanged(patientProfile.patient.age!!.value.toString())
        } else {
          DateOfBirthChanged(dateOfBirthFormat.format(patientProfile.patient.dateOfBirth!!))
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
      val inputEvents: List<EditPatientEvent>,
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

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(NameChanged(name))
    uiEvents.onNext(PhoneNumberChanged(enteredPhoneNumber))
    uiEvents.onNext(ColonyOrVillageChanged(colonyOrVillage))
    uiEvents.onNext(DistrictChanged(district))
    uiEvents.onNext(StateChanged(state))
    uiEvents.onNext(GenderChanged(Gender.Male))

    if (age != null) {
      uiEvents.onNext(DateOfBirthChanged(""))
      uiEvents.onNext(AgeChanged(age))
    }

    if (dateOfBirth != null) {
      uiEvents.onNext(AgeChanged(""))
      uiEvents.onNext(DateOfBirthChanged(dateOfBirth))
    }

    if (age == null && dateOfBirth == null) {
      uiEvents.onNext(AgeChanged(""))
    }

    uiEvents.onNext(SaveClicked)

    if (expectedErrors.isNotEmpty()) {
      // This is order dependent because finding the first field
      // with error is only possible once the errors are set.
      val inOrder = inOrder(ui)

      inOrder.verify(ui).showValidationErrors(expectedErrors)
      inOrder.verify(ui).scrollToFirstFieldWithError()

    } else {
      verify(ui, never()).showValidationErrors(any())
      verify(ui, never()).scrollToFirstFieldWithError()
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
      val expectedErrors: Set<EditPatientValidationError>,
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

    screenCreated(patient, address, alreadyPresentPhoneNumber)

    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(PhoneNumberChanged(enteredPhoneNumber))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(expectedError))
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
      val expectedError: EditPatientValidationError
  )

  @Test
  fun `when save is clicked and phone number is not already saved, entering a blank phone number should not show errors`() {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()

    whenever(patientRepository.updateAddressForPatient(eq(patient.uuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())

    screenCreated(patient, address, null)

    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui, never()).showValidationErrors(any())
  }

  private fun screenCreated(patient: Patient, address: PatientAddress, phoneNumber: PatientPhoneNumber?) {
    val fixture = MobiusTestFixture<EditPatientModel, EditPatientEvent, EditPatientEffect>(
        uiEvents,
        EditPatientModel.from(patient, address, phoneNumber, dateOfBirthFormat),
        EditPatientInit(patient, address, phoneNumber),
        EditPatientUpdate(IndianPhoneNumberValidator(), UserInputDateValidator(ZoneOffset.UTC, dateOfBirthFormat)),
        EditPatientEffectHandler.createEffectHandler(ui, TestUserClock(), patientRepository, utcClock, dateOfBirthFormat, TrampolineSchedulersProvider()),
        viewRenderer::render
    )

    fixture.start()
  }
}
