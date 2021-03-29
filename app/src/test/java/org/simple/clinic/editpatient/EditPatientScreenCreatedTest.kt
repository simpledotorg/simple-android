package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.editpatient.EditPatientState.NOT_SAVING_PATIENT
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.mobius.migration.MobiusTestFixture
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class EditPatientScreenCreatedTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui: EditPatientUi = mock()
  private val utcClock: TestUtcClock = TestUtcClock()
  private val userClock: TestUserClock = TestUserClock()
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val patientRepository = mock<PatientRepository>()
  private val country = TestData.country()
  private val user = TestData.loggedInUser()

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider(
      dateTimeFormatter = dateOfBirthFormat,
      today = LocalDate.now(userClock)
  ))

  @Test
  @Parameters(method = "params for prefilling fields on screen created")
  fun `when screen is created then the existing patient data must be prefilled`(patientFormTestParams: PatientFormTestParams) {
    val (patient, address, phoneNumber) = patientFormTestParams

    whenever(patientRepository.bangladeshNationalIdForPatient(patient.uuid)) doReturn Observable.never()

    val patientProfile = TestData.patientProfile(patientUuid = patient.uuid, generateBusinessId = true)
    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.just(patientProfile.toOptional())

    screenCreated(patient, address, phoneNumber)

    verify(ui).displayBpPassports(patientProfile.businessIds.map { it.identifier.displayValue() })

    if (patientFormTestParams.shouldSetColonyOrVillage) {
      verify(ui).setColonyOrVillage(address.colonyOrVillage!!)
    } else {
      verify(ui, never()).setColonyOrVillage(any())
    }

    verify(ui).setDistrict(address.district)
    verify(ui).setState(address.state)
    verify(ui).setGender(patient.gender)
    verify(ui).setPatientName(patient.fullName)

    if (patientFormTestParams.shouldSetPhoneNumber) {
      verify(ui).setPatientPhoneNumber(phoneNumber!!.number)
    } else {
      verify(ui, never()).setPatientPhoneNumber(any())
    }

    if (patientFormTestParams.shouldSetAge) {
      verify(ui).setPatientAge(any())
    } else {
      verify(ui, never()).setPatientAge(any())
    }

    if (patientFormTestParams.shouldSetDateOfBirth) {
      verify(ui).setPatientDateOfBirth(patient.dateOfBirth!!)
    } else {
      verify(ui, never()).setPatientDateOfBirth(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling fields on screen created`(): List<PatientFormTestParams> {
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
  ): PatientFormTestParams {
    val patientToReturn = TestData.patient(
        age = Age(23, Instant.now(utcClock)),
        dateOfBirth = null
    )
    val addressToReturn = TestData.patientAddress(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
    val phoneNumberToReturn = phoneNumber?.let { TestData.patientPhoneNumber(patientUuid = patientToReturn.uuid, number = it) }

    return PatientFormTestParams(
        patientToReturn,
        addressToReturn,
        phoneNumberToReturn
    )
  }

  private fun patientFormDataWithDateOfBirth(
      colonyOrVillage: String?,
      phoneNumber: String?,
      dateOfBirth: LocalDate
  ): PatientFormTestParams {
    val patientToReturn = TestData.patient(dateOfBirth = dateOfBirth, age = null)
    val addressToReturn = TestData.patientAddress(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
    val phoneNumberToReturn = phoneNumber?.let { TestData.patientPhoneNumber(patientUuid = patientToReturn.uuid, number = it) }

    return PatientFormTestParams(
        patientToReturn,
        addressToReturn,
        phoneNumberToReturn
    )
  }

  data class PatientFormTestParams(
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

  private fun screenCreated(patient: Patient, address: PatientAddress, phoneNumber: PatientPhoneNumber?) {
    val editPatientEffectHandler = EditPatientEffectHandler(
        userClock = TestUserClock(),
        patientRepository = patientRepository,
        utcClock = utcClock,
        schedulersProvider = TrampolineSchedulersProvider(),
        country = country,
        uuidGenerator = FakeUuidGenerator.fixed(UUID.fromString("4a08c52c-ebef-44a2-9de4-02916e703a47")),
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

    MobiusTestFixture<EditPatientModel, EditPatientEvent, EditPatientEffect>(
        events = Observable.never<EditPatientEvent>(),
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
        modelUpdateListener = { /* nothing here */ }
    ).start()
  }
}
