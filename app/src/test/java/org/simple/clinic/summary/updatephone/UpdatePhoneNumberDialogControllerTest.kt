package org.simple.clinic.summary.updatephone

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class UpdatePhoneNumberDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

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
    val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(phoneNumber)))

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))

    verify(dialog).preFillPhoneNumber(phoneNumber.number)
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    val newNumber = "1234567890"
    val existingPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid, number = "0987654321")

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(ValidNumber)
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
    val existingPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid, number = "old-number")

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)
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
    val existingPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid, number = "old-number")

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(any(), any())).thenReturn(Completable.never())

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    when (validationError) {
      Blank, is LengthTooShort -> verify(dialog).showPhoneNumberTooShortError()
      is LengthTooLong -> verify(dialog).showPhoneNumberTooLongError()
      ValidNumber -> throw AssertionError()
    }.exhaustive()
  }

  @Test
  fun `when cancel is clicked then the existing number should be saved again`() {
    val existingPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber)).thenReturn(Completable.complete())

    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberCancelClicked)

    verify(repository).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber)
  }

  @Suppress("unused")
  private fun `validation errors`() = listOf(Blank, LengthTooShort(6),LengthTooLong(12))
}
