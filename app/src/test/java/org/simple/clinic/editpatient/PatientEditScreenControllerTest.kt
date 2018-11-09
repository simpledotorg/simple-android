package org.simple.clinic.editpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.toOptional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()

  private lateinit var screen: PatientEditScreen
  private lateinit var patientRepository: PatientRepository
  private lateinit var numberValidator: PhoneNumberValidator
  private lateinit var controller: PatientEditScreenController

  @Before
  fun setUp() {
    screen = mock()
    patientRepository = mock()
    numberValidator = mock()

    controller = PatientEditScreenController(patientRepository, numberValidator)

    uiEvents
        .compose(controller)
        .subscribe { uiChange ->
          uiChange(screen)
        }
  }

  @Test
  @Parameters(method = "params for prefilling fields on screen created")
  fun `when screen is created then the existing patient data must be prefilled`(
      patient: Patient,
      address: PatientAddress,
      shouldSetColonyOrVillage: Boolean,
      phoneNumber: PatientPhoneNumber?,
      shouldSetPhoneNumber: Boolean
  ) {
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(Just(patient)))
    whenever(patientRepository.address(patient.addressUuid)).thenReturn(Observable.just(Just(address)))
    whenever(patientRepository.phoneNumbers(patient.uuid)).thenReturn(Observable.just(phoneNumber.toOptional()))

    uiEvents.onNext(PatientEditScreenCreated(patient.uuid))

    if (shouldSetColonyOrVillage) {
      verify(screen).setColonyOrVillage(address.colonyOrVillage!!)
    } else {
      verify(screen, never()).setColonyOrVillage(any())
    }

    verify(screen).setDistrict(address.district)
    verify(screen).setState(address.state)
    verify(screen).setGender(patient.gender)
    verify(screen).setPatientName(patient.fullName)

    if (shouldSetPhoneNumber) {
      verify(screen).setPatientPhoneNumber(phoneNumber!!.number)
    } else {
      verify(screen, never()).setPatientPhoneNumber(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling fields on screen created`(): List<List<Any?>> {
    fun generateTestData(colonyOrVillage: String?, phoneNumber: String?): List<Any?> {
      val patientToReturn = PatientMocker.patient()
      val addressToReturn = PatientMocker.address(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
      val phoneNumberToReturn = phoneNumber?.let { PatientMocker.phoneNumber(patientUuid = patientToReturn.uuid, number = it) }

      return listOf(
          patientToReturn,
          addressToReturn,
          colonyOrVillage.isNullOrBlank().not(),
          phoneNumberToReturn,
          phoneNumberToReturn != null
      )
    }

    return listOf(
        generateTestData("Colony", phoneNumber = "1111111111"),
        generateTestData(null, phoneNumber = "1111111111"),
        generateTestData("", phoneNumber = "1111111111"),
        generateTestData(colonyOrVillage = "Colony", phoneNumber = null)
    )
  }

  @Test
  fun `when save is clicked, patient name should be validated`() {
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(None))

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))

    uiEvents.onNext(PatientEditPatientNameTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(FULL_NAME_EMPTY))
  }

  @Test
  @Parameters(method = "params for validating phone numbers")
  fun `when save is clicked, phone number should be validated`(
      alreadyPresentPhoneNumber: PatientPhoneNumber?,
      numberValidationResult: PhoneNumberValidator.Result,
      expectedError: PatientEditValidationError?
  ) {
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(alreadyPresentPhoneNumber.toOptional()))
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))

    whenever(numberValidator.validate(any(), any())).thenReturn(numberValidationResult)

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))

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
        listOf(null, BLANK, null),
        listOf(null, LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_LONG),
        listOf(null, LENGTH_TOO_SHORT, PHONE_NUMBER_LENGTH_TOO_SHORT),
        listOf(PatientMocker.phoneNumber(), BLANK, PHONE_NUMBER_EMPTY),
        listOf(PatientMocker.phoneNumber(), LENGTH_TOO_SHORT, PHONE_NUMBER_LENGTH_TOO_SHORT),
        listOf(PatientMocker.phoneNumber(), LENGTH_TOO_LONG, PHONE_NUMBER_LENGTH_TOO_LONG)
    )
  }

  @Test
  fun `when save is clicked, the colony should be validated`() {
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(None))

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))

    uiEvents.onNext(PatientEditColonyOrVillageChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(COLONY_OR_VILLAGE_EMPTY))
  }

  @Test
  fun `when save is clicked, the district should be validated`() {
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(None))

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))

    uiEvents.onNext(PatientEditDistrictTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(DISTRICT_EMPTY))
  }

  @Test
  fun `when save is clicked, the state should be validated`() {
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(None))

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))

    uiEvents.onNext(PatientEditStateTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(STATE_EMPTY))
  }

  @Test
  @Parameters(method = "params for validating all fields on save clicks")
  fun `when save is clicked, all fields should be validated`(
      alreadyPresentPhoneNumber: PatientPhoneNumber?,
      name: String,
      numberValidationResult: PhoneNumberValidator.Result,
      colonyOrVillage: String,
      district: String,
      state: String,
      expectedErrors: Set<PatientEditValidationError>
  ) {
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(alreadyPresentPhoneNumber.toOptional()))
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))

    whenever(numberValidator.validate(any(), any())).thenReturn(numberValidationResult)

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditPatientNameTextChanged(name))
    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditColonyOrVillageChanged(colonyOrVillage))
    uiEvents.onNext(PatientEditDistrictTextChanged(district))
    uiEvents.onNext(PatientEditStateTextChanged(state))
    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))

    uiEvents.onNext(PatientEditSaveClicked())

    if (expectedErrors.isNotEmpty()) {
      verify(screen).showValidationErrors(expectedErrors)
    } else {
      verify(screen, never()).showValidationErrors(any())
    }
  }

  @Suppress("Unused")
  private fun `params for validating all fields on save clicks`(): List<List<Any?>> {
    return listOf(
        listOf(
            PatientMocker.phoneNumber(),
            "",
            BLANK,
            "",
            "",
            "",
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_EMPTY, COLONY_OR_VILLAGE_EMPTY, DISTRICT_EMPTY, STATE_EMPTY)
        ),
        listOf(
            null,
            "",
            BLANK,
            "",
            "",
            "",
            setOf(FULL_NAME_EMPTY, COLONY_OR_VILLAGE_EMPTY, DISTRICT_EMPTY, STATE_EMPTY)
        ),

        listOf(
            PatientMocker.phoneNumber(),
            "",
            LENGTH_TOO_SHORT,
            "Colony",
            "",
            "",
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, DISTRICT_EMPTY, STATE_EMPTY)
        ),
        listOf(
            null,
            "",
            LENGTH_TOO_SHORT,
            "Colony",
            "",
            "",
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, DISTRICT_EMPTY, STATE_EMPTY)
        ),

        listOf(
            PatientMocker.phoneNumber(),
            "Name",
            LENGTH_TOO_LONG,
            "",
            "District",
            "",
            setOf(PHONE_NUMBER_LENGTH_TOO_LONG, COLONY_OR_VILLAGE_EMPTY, STATE_EMPTY)
        ),
        listOf(
            null,
            "Name",
            LENGTH_TOO_LONG,
            "",
            "District",
            "",
            setOf(PHONE_NUMBER_LENGTH_TOO_LONG, COLONY_OR_VILLAGE_EMPTY, STATE_EMPTY)
        ),

        listOf(
            PatientMocker.phoneNumber(),
            "",
            VALID,
            "Colony",
            "District",
            "",
            setOf(FULL_NAME_EMPTY, STATE_EMPTY)
        ),
        listOf(
            null,
            "",
            VALID,
            "Colony",
            "District",
            "",
            setOf(FULL_NAME_EMPTY, STATE_EMPTY)
        ),

        listOf(
            PatientMocker.phoneNumber(),
            "",
            BLANK,
            "Colony",
            "District",
            "State",
            setOf(FULL_NAME_EMPTY, PHONE_NUMBER_EMPTY)
        ),
        listOf(
            null,
            "",
            BLANK,
            "Colony",
            "District",
            "State",
            setOf(FULL_NAME_EMPTY)
        ),

        listOf(
            PatientMocker.phoneNumber(),
            "Name",
            VALID,
            "Colony",
            "District",
            "State",
            emptySet<PatientEditValidationError>()
        ),
        listOf(
            null,
            "Name",
            VALID,
            "Colony",
            "District",
            "State",
            emptySet<PatientEditValidationError>()
        )
    )
  }

  @Test
  @Parameters(method = "params for hiding errors on text changes")
  fun `when input changes, errors corresponding to the input must be hidden`(
      inputChange: UiEvent,
      expectedErrorsToHide: Set<PatientEditValidationError>
  ) {
    whenever(patientRepository.phoneNumbers(any())).thenReturn(Observable.just(None))
    whenever(patientRepository.patient(any())).thenReturn(Observable.just(PatientMocker.patient().toOptional()))
    whenever(patientRepository.address(any())).thenReturn(Observable.just(PatientMocker.address().toOptional()))

    whenever(numberValidator.validate(any(), any())).thenReturn(BLANK)

    uiEvents.onNext(PatientEditScreenCreated(UUID.randomUUID()))

    uiEvents.onNext(PatientEditSaveClicked())

    uiEvents.onNext(inputChange)

    if (expectedErrorsToHide.isNotEmpty()) {
      verify(screen).hideValidationErrors(expectedErrorsToHide)
    } else {
      verify(screen, never()).hideValidationErrors(any())
    }
  }

  @Suppress("Unused")
  private fun `params for hiding errors on text changes`(): List<List<Any>> {
    return listOf(
        listOf(PatientEditPatientNameTextChanged(""), setOf(FULL_NAME_EMPTY)),
        listOf(PatientEditPatientNameTextChanged("Name"), setOf(FULL_NAME_EMPTY)),
        listOf(PatientEditPhoneNumberTextChanged(""), setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, PHONE_NUMBER_LENGTH_TOO_LONG)),
        listOf(PatientEditPhoneNumberTextChanged("12345"), setOf(PHONE_NUMBER_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, PHONE_NUMBER_LENGTH_TOO_LONG)),
        listOf(PatientEditColonyOrVillageChanged(""), setOf(COLONY_OR_VILLAGE_EMPTY)),
        listOf(PatientEditColonyOrVillageChanged("Colony"), setOf(COLONY_OR_VILLAGE_EMPTY)),
        listOf(PatientEditStateTextChanged(""), setOf(STATE_EMPTY)),
        listOf(PatientEditStateTextChanged("State"), setOf(STATE_EMPTY)),
        listOf(PatientEditDistrictTextChanged(""), setOf(DISTRICT_EMPTY)),
        listOf(PatientEditDistrictTextChanged("District"), setOf(DISTRICT_EMPTY)),
        listOf(PatientEditGenderChanged(Gender.TRANSGENDER), emptySet<PatientEditValidationError>())
    )
  }
}
