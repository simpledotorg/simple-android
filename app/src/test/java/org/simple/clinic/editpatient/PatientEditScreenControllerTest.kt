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
import org.simple.clinic.editpatient.PatientEditValidationError.*
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.*
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

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
    whenever(patientRepository.phoneNumbers(patient.uuid)).thenReturn(Observable.just(Optional.toOptional(phoneNumber)))

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
  @Parameters(value = [
  "BLANK|PHONE_NUMBER_EMPTY",
  "LENGTH_TOO_LONG|PHONE_NUMBER_LENGTH_TOO_LONG",
  "LENGTH_TOO_SHORT|PHONE_NUMBER_LENGTH_TOO_SHORT"
  ])
  fun `when save is clicked, phone number should be validated`(
      numberValidationResult: PhoneNumberValidator.Result,
      expectedError: PatientEditValidationError
  ) {
    whenever(numberValidator.validate(any(), any())).thenReturn(numberValidationResult)

    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))
    uiEvents.onNext(PatientEditColonyOrVillageChanged("Colony"))
    uiEvents.onNext(PatientEditDistrictTextChanged("District"))
    uiEvents.onNext(PatientEditStateTextChanged("State"))
    uiEvents.onNext(PatientEditPatientNameTextChanged("Name"))

    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditSaveClicked())

    verify(screen).showValidationErrors(setOf(expectedError))
  }

  @Test
  fun `when save is clicked, the colony should be validated`() {
    whenever(numberValidator.validate(any(), any())).thenReturn(VALID)

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
      name: String,
      numberValidationResult: PhoneNumberValidator.Result,
      colonyOrVillage: String,
      district: String,
      state: String,
      expectedErrors: Set<PatientEditValidationError>
  ) {
    whenever(numberValidator.validate(any(), any())).thenReturn(numberValidationResult)

    uiEvents.onNext(PatientEditPatientNameTextChanged(name))
    uiEvents.onNext(PatientEditPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientEditColonyOrVillageChanged(colonyOrVillage))
    uiEvents.onNext(PatientEditDistrictTextChanged(district))
    uiEvents.onNext(PatientEditStateTextChanged(state))
    uiEvents.onNext(PatientEditGenderChanged(Gender.MALE))

    uiEvents.onNext(PatientEditSaveClicked())

    if(expectedErrors.isNotEmpty()) {
      verify(screen).showValidationErrors(expectedErrors)
    } else {
      verify(screen, never()).showValidationErrors(any())
    }
  }

  @Suppress("Unused")
  private fun `params for validating all fields on save clicks`(): List<List<Any>> {
    return listOf(
        listOf("", BLANK, "", "", "", setOf(FULL_NAME_EMPTY, PHONE_NUMBER_EMPTY, COLONY_OR_VILLAGE_EMPTY, DISTRICT_EMPTY, STATE_EMPTY)),
        listOf("", LENGTH_TOO_SHORT, "Colony", "", "", setOf(FULL_NAME_EMPTY, PHONE_NUMBER_LENGTH_TOO_SHORT, DISTRICT_EMPTY, STATE_EMPTY)),
        listOf("Name", LENGTH_TOO_LONG, "", "District", "", setOf(PHONE_NUMBER_LENGTH_TOO_LONG, COLONY_OR_VILLAGE_EMPTY, STATE_EMPTY)),
        listOf("", VALID, "Colony", "District", "", setOf(FULL_NAME_EMPTY, STATE_EMPTY)),
        listOf("", BLANK, "Colony", "District", "State", setOf(FULL_NAME_EMPTY, PHONE_NUMBER_EMPTY)),
        listOf("Name", VALID, "Colony", "District", "State", emptySet<PatientEditValidationError>())
    )
  }
}
