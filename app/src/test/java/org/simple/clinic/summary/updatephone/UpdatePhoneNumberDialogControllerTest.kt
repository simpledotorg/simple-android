package org.simple.clinic.summary.updatephone

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
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

  private val patientUuid = UUID.fromString("0c1c5a00-2416-4a41-8b9e-8059ac18df5d")

  private lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `when dialog is created, the existing phone number should be pre-filled`() {
    val phoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("4ada8db2-71dc-4a3b-8d17-69032cab2155"),
        patientUuid = patientUuid
    )
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(phoneNumber)))

    setupController()
    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))

    verify(dialog).preFillPhoneNumber(phoneNumber.number)
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    val newNumber = "1234567890"
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("104e74c7-381a-4c07-8728-d6db77086dd3"),
        patientUuid = patientUuid,
        number = "0987654321"
    )

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(ValidNumber)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.copy(number = newNumber))).thenReturn(Completable.complete())

    setupController()
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
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("ab3f84b5-683f-49ae-987a-e319fd1db7d2"),
        patientUuid = patientUuid,
        number = "1234567890"
    )

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.copy(number = newNumber))).thenReturn(Completable.complete())

    setupController()
    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    verify(repository, never()).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.copy(number = newNumber))
  }

  @Test
  @Parameters(method = "validation errors")
  fun `when save is clicked, an error should be shown if it's invalid`(
      validationError: Result
  ) {
    val newNumber = "123"
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("0e4bf753-009b-4cd6-ae30-aa9935bf2ea6"),
        patientUuid = patientUuid,
        number = "1234567890"
    )

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.copy(number = newNumber))).thenReturn(Completable.never())

    setupController()
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

    setupController()
    uiEvents.onNext(UpdatePhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(UpdatePhoneNumberCancelClicked)

    verify(repository).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber)
  }

  @Suppress("unused")
  private fun `validation errors`() = listOf(Blank, LengthTooShort(6), LengthTooLong(12))

  private fun setupController() {
    val controller = UpdatePhoneNumberDialogController(repository, validator)

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }
}
