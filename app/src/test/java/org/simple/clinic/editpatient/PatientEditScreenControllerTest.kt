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
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

@RunWith(JUnitParamsRunner::class)
class PatientEditScreenControllerTest {

  private val screen = mock<PatientEditScreen>()
  private val patientRepository = mock<PatientRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = PatientEditScreenController(patientRepository)

  @Before
  fun setUp() {

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
}
