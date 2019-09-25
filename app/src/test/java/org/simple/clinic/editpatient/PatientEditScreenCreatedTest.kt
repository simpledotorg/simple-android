package org.simple.clinic.editpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenCreatedTest {

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
  @Parameters(method = "params for prefilling fields on screen created")
  fun `when screen is created then the existing patient data must be prefilled`(patientFormData: PatientFormData) {
    val (patient, address, phoneNumber) = patientFormData

    uiEvents.onNext(PatientEditScreenCreated.fromPatientData(patient, address, phoneNumber))

    if (patientFormData.shouldSetColonyOrVillage) {
      verify(screen).setColonyOrVillage(address.colonyOrVillage!!)
    } else {
      verify(screen, never()).setColonyOrVillage(any())
    }

    verify(screen).setDistrict(address.district)
    verify(screen).setState(address.state)
    verify(screen).setGender(patient.gender)
    verify(screen).setPatientName(patient.fullName)

    if (patientFormData.shouldSetPhoneNumber) {
      verify(screen).setPatientPhoneNumber(phoneNumber!!.number)
    } else {
      verify(screen, never()).setPatientPhoneNumber(any())
    }

    if (patientFormData.shouldSetAge) {
      verify(screen).setPatientAge(any())
    } else {
      verify(screen, never()).setPatientAge(any())
    }

    if (patientFormData.shouldSetDateOfBirth) {
      verify(screen).setPatientDateofBirth(patient.dateOfBirth!!)
    } else {
      verify(screen, never()).setPatientDateofBirth(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling fields on screen created`(): List<PatientFormData> {
    return listOf(
        patientFormDataWithAge(colonyOrVillage = "Colony", phoneNumber = "1111111111"),
        patientFormDataWithAge(colonyOrVillage = null, phoneNumber = "1111111111"),
        patientFormDataWithAge(colonyOrVillage = "", phoneNumber = "1111111111"),
        patientFormDataWithAge(colonyOrVillage = "Colony", phoneNumber = null),
        patientFormDataWithDateOfBirth(colonyOrVillage = "Colony", phoneNumber = null, dateOfBirth = LocalDate.parse("1995-11-28"))
    )
  }

  private fun patientFormDataWithAge(
      colonyOrVillage: String?,
      phoneNumber: String?
  ): PatientFormData {
    val patientToReturn = PatientMocker.patient(
        age = Age(23, Instant.now(utcClock)),
        dateOfBirth = null
    )
    val addressToReturn = PatientMocker.address(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
    val phoneNumberToReturn = phoneNumber?.let { PatientMocker.phoneNumber(patientUuid = patientToReturn.uuid, number = it) }

    return PatientFormData(
        patientToReturn,
        addressToReturn,
        phoneNumberToReturn
    )
  }

  private fun patientFormDataWithDateOfBirth(
      colonyOrVillage: String?,
      phoneNumber: String?,
      dateOfBirth: LocalDate
  ): PatientFormData {
    val patientToReturn = PatientMocker.patient(dateOfBirth = dateOfBirth, age = null)
    val addressToReturn = PatientMocker.address(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
    val phoneNumberToReturn = phoneNumber?.let { PatientMocker.phoneNumber(patientUuid = patientToReturn.uuid, number = it) }

    return PatientFormData(
        patientToReturn,
        addressToReturn,
        phoneNumberToReturn
    )
  }
}

data class PatientFormData(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) {
  val shouldSetColonyOrVillage: Boolean
    get() = address.colonyOrVillage.isNullOrBlank().not()

  val shouldSetPhoneNumber: Boolean
    get() = phoneNumber != null

  val shouldSetAge: Boolean
    get() = patient.age != null

  val shouldSetDateOfBirth: Boolean
    get() = patient.dateOfBirth != null
}
