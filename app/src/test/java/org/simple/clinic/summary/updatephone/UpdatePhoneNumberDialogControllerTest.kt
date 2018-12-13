package org.simple.clinic.summary.updatephone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
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
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class UpdatePhoneNumberDialogControllerTest {

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val dialog = mock<UpdatePhoneNumberDialog>()
  private val repository = mock<PatientRepository>()
  private val validator = mock<PhoneNumberValidator>()

  private val patientUuid = UUID.randomUUID()

  private lateinit var controller: UpdatePhoneNumberDialogController

  @Before
  fun setup() {
    controller = UpdatePhoneNumberDialogController(repository, validator)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when dialog is created, the existing phone number should be pre-filled`() {
    val phoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(phoneNumber)))

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))

    verify(dialog).preFillPhoneNumber(phoneNumber.number)
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    val newNumber = "1234567890"
    val existingPhoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid, number = "0987654321")

    whenever(validator.validate(newNumber, type = MOBILE)).thenReturn(Result.VALID)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    verify(repository).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.copy(number = newNumber))
  }

  @Test
  @Parameters(method = "validation errors")
  fun `when save is clicked, the number should not be saved if it's invalid`(
      validationError: Result
  ) {
    val newNumber = "123"
    val existingPhoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid, number = "old-number")

    whenever(validator.validate(newNumber, type = MOBILE)).thenReturn(validationError)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(eq(patientUuid), any())).thenReturn(Completable.complete())

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    verify(repository, never()).updatePhoneNumberForPatient(any(), any())
  }

  @Test
  @Parameters(method = "validation errors")
  fun `when save is clicked, an error should be shown if it's invalid`(
      validationError: Result
  ) {
    val newNumber = "123"
    val existingPhoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid, number = "old-number")

    whenever(validator.validate(newNumber, type = MOBILE)).thenReturn(validationError)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(any(), any())).thenReturn(Completable.never())

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    verify(dialog).showIncompletePhoneNumberError()
  }

  @Suppress("unused")
  private fun `validation errors`() = Result.values().filter { it != Result.VALID }
}
