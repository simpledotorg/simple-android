package org.simple.clinic.editpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenValidationUsingMockNumberValidatorTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  val utcClock: TestUtcClock = TestUtcClock()

  private lateinit var screen: PatientEditScreen
  private lateinit var patientRepository: PatientRepository
  private lateinit var numberValidator: PhoneNumberValidator
  private lateinit var controller: PatientEditScreenController

  private lateinit var errorConsumer: (Throwable) -> Unit
  private lateinit var dobValidator: UserInputDateValidator
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  @Before
  fun setUp() {
    screen = mock()
    patientRepository = mock()
    numberValidator = mock()
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
  @Parameters(method = "params for validating all fields on save clicks")
  fun `when save is clicked, all fields should be validated`(validateFieldsData: ValidateFieldsData) {
    val (alreadyPresentPhoneNumber,
        name,
        numberValidationResult,
        colonyOrVillage,
        district,
        state,
        age,
        userInputDateOfBirthValidationResult,
        dateOfBirth,
        expectedErrors
    ) = validateFieldsData

    val patient = PatientMocker.patient()
    val address = PatientMocker.address()

    whenever(patientRepository.updatePhoneNumberForPatient(any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())

    whenever(numberValidator.validate(any(), any())).thenReturn(numberValidationResult)
    if (userInputDateOfBirthValidationResult != null) {
      whenever(dobValidator.validate(any(), any())).thenReturn(userInputDateOfBirthValidationResult)
    }

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, alreadyPresentPhoneNumber))

    uiEvents.onNext(PatientEditPatientNameTextChanged(name))
    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
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
  private fun `params for validating all fields on save clicks`(): List<ValidateFieldsData> {
    return listOf(
        ValidateFieldsData(
            PatientMocker.phoneNumber(),
            "",
            PhoneNumberValidator.Result.BLANK,
            "",
            "",
            "",
            "1",
            null,
            null,
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.PHONE_NUMBER_EMPTY, PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY, PatientEditValidationError.DISTRICT_EMPTY, PatientEditValidationError.STATE_EMPTY)
        ),
        ValidateFieldsData(
            null,
            "",
            PhoneNumberValidator.Result.BLANK,
            "",
            "",
            "",
            "",
            null,
            null,
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY, PatientEditValidationError.DISTRICT_EMPTY, PatientEditValidationError.STATE_EMPTY, PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT)
        ),
        ValidateFieldsData(
            PatientMocker.phoneNumber(),
            "",
            PhoneNumberValidator.Result.LENGTH_TOO_SHORT,
            "Colony",
            "",
            "",
            "1",
            null,
            null,
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT, PatientEditValidationError.DISTRICT_EMPTY, PatientEditValidationError.STATE_EMPTY)
        ),
        ValidateFieldsData(
            null,
            "",
            PhoneNumberValidator.Result.LENGTH_TOO_SHORT,
            "Colony",
            "",
            "",
            "",
            null,
            null,
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT, PatientEditValidationError.DISTRICT_EMPTY, PatientEditValidationError.STATE_EMPTY, PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT)
        ),
        ValidateFieldsData(
            PatientMocker.phoneNumber(),
            "Name",
            PhoneNumberValidator.Result.LENGTH_TOO_LONG,
            "",
            "District",
            "",
            "1",
            null,
            null,
            setOf(PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG, PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY, PatientEditValidationError.STATE_EMPTY)
        ),
        ValidateFieldsData(
            null,
            "Name",
            PhoneNumberValidator.Result.LENGTH_TOO_LONG,
            "",
            "District",
            "",
            null,
            UserInputDateValidator.Result.Invalid.InvalidPattern,
            "01/01/2000",
            setOf(PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG, PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY, PatientEditValidationError.STATE_EMPTY, PatientEditValidationError.INVALID_DATE_OF_BIRTH)
        ),
        ValidateFieldsData(
            PatientMocker.phoneNumber(),
            "",
            PhoneNumberValidator.Result.VALID,
            "Colony",
            "District",
            "",
            null,
            null,
            null,
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.STATE_EMPTY, PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT)
        ),
        ValidateFieldsData(
            null,
            "",
            PhoneNumberValidator.Result.VALID,
            "Colony",
            "District",
            "",
            null,
            UserInputDateValidator.Result.Invalid.DateIsInFuture,
            "01/01/2000",
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.STATE_EMPTY, PatientEditValidationError.DATE_OF_BIRTH_IN_FUTURE)
        ),
        ValidateFieldsData(
            null,
            "",
            PhoneNumberValidator.Result.BLANK,
            "Colony",
            "District",
            "State",
            "",
            null,
            null,
            setOf(PatientEditValidationError.FULL_NAME_EMPTY, PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT)
        ),
        ValidateFieldsData(
            PatientMocker.phoneNumber(),
            "Name",
            PhoneNumberValidator.Result.VALID,
            "Colony",
            "District",
            "State",
            "1",
            null,
            null,
            emptySet<PatientEditValidationError>()
        ),
        ValidateFieldsData(
            null,
            "Name",
            PhoneNumberValidator.Result.VALID,
            "Colony",
            "District",
            "State",
            null,
            UserInputDateValidator.Result.Valid(LocalDate.parse("1947-01-01")),
            "01/01/2000",
            emptySet<PatientEditValidationError>()
        )
    )
  }

  @Test
  @Parameters(method = "params for validating phone numbers")
  fun `when save is clicked, phone number should be validated`(
      alreadyPresentPhoneNumber: PatientPhoneNumber?,
      numberValidationResult: PhoneNumberValidator.Result,
      expectedError: PatientEditValidationError?
  ) {
    val patient = PatientMocker.patient()
    val address = PatientMocker.address()
    val phoneNumber = alreadyPresentPhoneNumber

    whenever(patientRepository.patient(patient.uuid)).thenReturn(Observable.just(patient.toOptional()))
    whenever(patientRepository.address(address.uuid)).thenReturn(Observable.just(address.toOptional()))
    whenever(patientRepository.phoneNumber(patient.uuid)).thenReturn(Observable.just(phoneNumber.toOptional()))

    whenever(numberValidator.validate(any(), any())).thenReturn(numberValidationResult)

    whenever(patientRepository.updatePhoneNumberForPatient(eq(patient.uuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.createPhoneNumberForPatient(eq(patient.uuid), any(), any(), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(eq(patient.uuid), any())).thenReturn(Completable.complete())
    whenever(patientRepository.updatePatient(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientEditScreenCreated.from(patient, address, phoneNumber))

    uiEvents.onNext(PatientEditGenderChanged(Gender.Male))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))
    uiEvents.onNext(PatientEditAgeTextChanged("1"))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    if (expectedError == null) {
      verify(screen, never()).showValidationErrors(any())
    } else {
      verify(screen).showValidationErrors(setOf(expectedError))
    }
  }

  @Suppress("Unused")
  private fun `params for validating phone numbers`(): List<List<Any?>> {
    return listOf(
        listOf(null, PhoneNumberValidator.Result.BLANK, null),
        listOf(null, PhoneNumberValidator.Result.LENGTH_TOO_LONG, PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG),
        listOf(null, PhoneNumberValidator.Result.LENGTH_TOO_SHORT, PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT),
        listOf(PatientMocker.phoneNumber(), PhoneNumberValidator.Result.BLANK, PatientEditValidationError.PHONE_NUMBER_EMPTY),
        listOf(PatientMocker.phoneNumber(), PhoneNumberValidator.Result.LENGTH_TOO_SHORT, PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT),
        listOf(PatientMocker.phoneNumber(), PhoneNumberValidator.Result.LENGTH_TOO_LONG, PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG)
    )
  }
}

data class ValidateFieldsData(
    val alreadyPresentPhoneNumber: PatientPhoneNumber?,
    val name: String,
    val numberValidationResult: PhoneNumberValidator.Result,
    val colonyOrVillage: String,
    val district: String,
    val state: String,
    val age: String?,
    val userInputDateOfBirthValidationResult: UserInputDateValidator.Result?,
    val dateOfBirth: String?,
    val expectedErrors: Set<PatientEditValidationError>
)