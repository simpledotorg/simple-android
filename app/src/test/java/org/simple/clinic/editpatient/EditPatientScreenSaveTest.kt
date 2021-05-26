package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.editpatient.EditPatientState.NOT_SAVING_PATIENT
import org.simple.clinic.editpatient.EditPatientValidationError.BothDateOfBirthAndAgeAdsent
import org.simple.clinic.editpatient.EditPatientValidationError.ColonyOrVillageEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthInFuture
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthParseError
import org.simple.clinic.editpatient.EditPatientValidationError.DistrictEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.FullNameEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooLong
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.editpatient.EditPatientValidationError.StateEmpty
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PhoneNumberDetails
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
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
  private val country = TestData.country()

  private val utcClock: TestUtcClock = TestUtcClock()
  private val userClock: TestUserClock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val nextYear = "2019"
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val generatedPhoneUuid = UUID.fromString("ada3ea24-819b-42e4-ac21-51bcf61cebac")
  private val user = TestData.loggedInUser()

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider(
      dateTimeFormatter = dateOfBirthFormat,
      today = LocalDate.now(userClock)
  ))

  @Test
  fun `when save is clicked, patient name should be validated`() {
    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(NameChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(FullNameEmpty))
  }

  @Test
  fun `when save is clicked, the colony should be validated`() {
    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()
    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(ColonyOrVillageChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(ColonyOrVillageEmpty))
  }

  @Test
  fun `when save is clicked, the district should be validated`() {
    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(DistrictChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(DistrictEmpty))
  }

  @Test
  fun `when save is clicked, the state should be validated`() {
    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

    screenCreated(patient, address, phoneNumber)

    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(AgeChanged("1"))

    uiEvents.onNext(StateChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(StateEmpty))
  }

  @Test
  fun `when save is clicked, the age should be validated`() {
    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

    screenCreated(patient, address, phoneNumber)
    uiEvents.onNext(PhoneNumberChanged(""))
    uiEvents.onNext(GenderChanged(Gender.Male))
    uiEvents.onNext(ColonyOrVillageChanged("Colony"))
    uiEvents.onNext(DistrictChanged("District"))
    uiEvents.onNext(NameChanged("Name"))
    uiEvents.onNext(StateChanged("State"))
    uiEvents.onNext(AgeChanged(""))
    uiEvents.onNext(SaveClicked)

    verify(ui).showValidationErrors(setOf(BothDateOfBirthAndAgeAdsent))
  }

  @Test
  @Parameters(method = "params for date of birth should be validated")
  fun `when save is clicked, the date of birth should be validated`(
      dateOfBirthTestParams: DateOfBirthTestParams
  ) {
    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val phoneNumber: PatientPhoneNumber? = null

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

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
        DateOfBirthTestParams("20/40-80", InvalidPattern, DateOfBirthParseError),
        DateOfBirthTestParams("01/01/$nextYear", DateIsInFuture, DateOfBirthInFuture)
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
    whenever(patientRepository.createPhoneNumberForPatient(eq(generatedPhoneUuid), eq(patientUuid), any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.bangladeshNationalIdForPatient(patientUuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.never()

    utcClock.advanceBy(advanceClockBy)
    screenCreated(existingSavedPatient, existingSavedAddress, existingSavedPhoneNumber)
    inputEvents.forEach { uiEvents.onNext(it) }
    uiEvents.onNext(SaveClicked)

    if (!shouldSavePatient) {
      verify(patientRepository, never()).updatePatient(any())
      verify(patientRepository, never()).updateAddressForPatient(any(), any())
      verify(patientRepository, never()).updatePhoneNumberForPatient(any(), any())
      verify(patientRepository, never()).createPhoneNumberForPatient(any(), any(), any(), any())
      verify(ui, never()).goBack()
      return
    }

    verify(patientRepository).updatePatient(expectedSavedPatient!!)
    verify(patientRepository).updateAddressForPatient(expectedSavedPatient.uuid, expectedSavedPatientAddress!!)

    if (expectedSavedPatientPhoneNumber != null) {
      if (existingSavedPhoneNumber == null) {
        val numberDetails = PhoneNumberDetails.mobile(expectedSavedPatientPhoneNumber.number)

        verify(patientRepository).createPhoneNumberForPatient(
            uuid = generatedPhoneUuid,
            patientUuid = expectedSavedPatientPhoneNumber.patientUuid,
            numberDetails = numberDetails,
            active = true
        )
      } else {
        verify(patientRepository).updatePhoneNumberForPatient(expectedSavedPatient.uuid, expectedSavedPatientPhoneNumber)
      }

    } else {
      verify(patientRepository, never()).createPhoneNumberForPatient(any(), any(), any(), any())
      verify(patientRepository, never()).updatePhoneNumberForPatient(any(), any())
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
              alreadyPresentPhoneNumber?.copy(number = "12345678")
                  ?: TestData.patientPhoneNumber(patientUuid = patientId, number = "12345678")
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
              alreadyPresentPhoneNumber?.copy(number = "12345678")
                  ?: TestData.patientPhoneNumber(patientUuid = patientId, number = "12345678")
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
              alreadyPresentPhoneNumber?.copy(number = "123456")
                  ?: TestData.patientPhoneNumber(patientUuid = patientId, number = "123456")
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
              alreadyPresentPhoneNumber?.copy(number = "123456")
                  ?: TestData.patientPhoneNumber(patientUuid = patientId, number = "123456")
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
              alreadyPresentPhoneNumber?.copy(number = "123456")
                  ?: TestData.patientPhoneNumber(patientUuid = patientId, number = "123456")
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
              alreadyPresentPhoneNumber?.copy(number = "1234567")
                  ?: TestData.patientPhoneNumber(patientUuid = patientId, number = "1234567")
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

  private fun createPatientProfile(
      shouldAddNumber: Boolean,
      shouldHaveAge: Boolean
  ): PatientProfile {
    val patientUuid = UUID.randomUUID()
    val addressUuid = UUID.randomUUID()

    val patient = if (shouldHaveAge) {
      TestData.patient(
          uuid = patientUuid,
          age = Age(20, Instant.now(utcClock)),
          dateOfBirth = null,
          addressUuid = addressUuid)

    } else {
      TestData.patient(
          uuid = patientUuid,
          age = null,
          dateOfBirth = LocalDate.now(utcClock),
          addressUuid = addressUuid
      )
    }

    return PatientProfile(
        patient = patient,
        address = TestData.patientAddress(uuid = addressUuid),
        phoneNumbers = if (shouldAddNumber) listOf(TestData.patientPhoneNumber(patientUuid = patientUuid)) else emptyList(),
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

    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val patientUuid = patient.uuid
    val phoneNumber = alreadyPresentPhoneNumber?.copy(patientUuid = patientUuid)

    whenever(patientRepository.createPhoneNumberForPatient(eq(generatedPhoneUuid), eq(patientUuid), any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePhoneNumberForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())
    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

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
            TestData.patientPhoneNumber(),
            "",
            "",
            "",
            "",
            "1",
            null,
            setOf(FullNameEmpty, PhoneNumberEmpty, ColonyOrVillageEmpty, DistrictEmpty, StateEmpty),
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
            setOf(FullNameEmpty, ColonyOrVillageEmpty, DistrictEmpty, StateEmpty, BothDateOfBirthAndAgeAdsent),
            enteredPhoneNumber = "1234567890"
        ),
        ValidateFieldsTestParams(
            TestData.patientPhoneNumber(),
            "",
            "Colony",
            "",
            "",
            "1",
            null,
            setOf(FullNameEmpty, PhoneNumberLengthTooShort(6), DistrictEmpty, StateEmpty),
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
            setOf(FullNameEmpty, PhoneNumberLengthTooShort(6), DistrictEmpty, StateEmpty, BothDateOfBirthAndAgeAdsent),
            enteredPhoneNumber = "1234"
        ),
        ValidateFieldsTestParams(
            TestData.patientPhoneNumber(),
            "Name",
            "",
            "District",
            "",
            "1",
            null,
            setOf(PhoneNumberLengthTooLong(12), ColonyOrVillageEmpty, StateEmpty),
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
            setOf(PhoneNumberLengthTooLong(12), ColonyOrVillageEmpty, StateEmpty, DateOfBirthParseError),
            "12345678901234"
        ),
        ValidateFieldsTestParams(
            TestData.patientPhoneNumber(),
            "",
            "Colony",
            "District",
            "",
            null,
            null,
            setOf(FullNameEmpty, StateEmpty, BothDateOfBirthAndAgeAdsent),
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
            setOf(FullNameEmpty, StateEmpty, DateOfBirthInFuture),
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
            setOf(FullNameEmpty, BothDateOfBirthAndAgeAdsent),
            "12334567890"
        ),
        ValidateFieldsTestParams(
            TestData.patientPhoneNumber(),
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

    val patient = TestData.patient()
    val address = TestData.patientAddress()
    val patientUuid = patient.uuid

    whenever(patientRepository.createPhoneNumberForPatient(eq(generatedPhoneUuid), eq(patientUuid), any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.bangladeshNationalIdForPatient(patientUuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patientUuid)) doReturn Observable.never()

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
        ValidatePhoneNumberTestParams(null, "1234", PhoneNumberLengthTooShort(6)),
        ValidatePhoneNumberTestParams(null, "12345678901234", PhoneNumberLengthTooLong(12)),
        ValidatePhoneNumberTestParams(TestData.patientPhoneNumber(), "12345678901234", PhoneNumberLengthTooLong(12)),
        ValidatePhoneNumberTestParams(TestData.patientPhoneNumber(), "", PhoneNumberEmpty),
        ValidatePhoneNumberTestParams(TestData.patientPhoneNumber(), "1234", PhoneNumberLengthTooShort(6))
    )
  }

  data class ValidatePhoneNumberTestParams(
      val alreadyPresentPhoneNumber: PatientPhoneNumber?,
      val enteredPhoneNumber: String,
      val expectedError: EditPatientValidationError
  )

  @Test
  fun `when save is clicked and phone number is not already saved, entering a blank phone number should not show errors`() {
    val patient = TestData.patient()
    val address = TestData.patientAddress()

    whenever(patientRepository.updateAddressForPatient(eq(patient.uuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())
    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.never()

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

  private fun screenCreated(
      patient: Patient,
      address: PatientAddress,
      phoneNumber: PatientPhoneNumber?
  ) {
    val editPatientEffectHandler = EditPatientEffectHandler(
        userClock = TestUserClock(),
        patientRepository = patientRepository,
        utcClock = utcClock,
        schedulersProvider = TrampolineSchedulersProvider(),
        country = country,
        uuidGenerator = FakeUuidGenerator.fixed(generatedPhoneUuid),
        currentUser = dagger.Lazy { user },
        inputFieldsFactory = inputFieldsFactory,
        dateOfBirthFormatter = dateOfBirthFormat,
        ui = ui
    )

    val numberValidator = LengthBasedNumberValidator(
        minimumRequiredLengthMobile = 10,
        maximumAllowedLengthMobile = 10,
        minimumRequiredLengthLandlinesOrMobile = 6,
        maximumAllowedLengthLandlinesOrMobile = 12
    )

    val fixture = MobiusTestFixture<EditPatientModel, EditPatientEvent, EditPatientEffect>(
        events = uiEvents,
        defaultModel = EditPatientModel.from(patient, address, phoneNumber, dateOfBirthFormat, null, NOT_SAVING_PATIENT),
        init = EditPatientInit(patient = patient,
            address = address,
            phoneNumber = phoneNumber,
            bangladeshNationalId = null,
            isVillageTypeAheadEnabled = true),
        update = EditPatientUpdate(
            numberValidator = numberValidator,
            dobValidator = UserInputDateValidator(userClock, dateOfBirthFormat),
            ageValidator = UserInputAgeValidator(userClock, dateOfBirthFormat)
        ),
        effectHandler = editPatientEffectHandler.build(),
        modelUpdateListener = viewRenderer::render
    )

    fixture.start()
  }
}
