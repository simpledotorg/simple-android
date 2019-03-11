package org.simple.clinic.summary.addphone

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientPhoneNumberType.MOBILE
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.values
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class AddPhoneNumberDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val dialog = mock<AddPhoneNumberDialog>()
  private val repository = mock<PatientRepository>()
  private val validator = mock<PhoneNumberValidator>()

  private val patientUuid = UUID.randomUUID()

  private lateinit var controller: AddPhoneNumberDialogController

  @Before
  fun setup() {
    controller = AddPhoneNumberDialogController(repository, validator)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    val newNumber = "1234567890"
    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(VALID)
    whenever(repository.createPhoneNumberForPatient(any(), any(), any(), any())).thenReturn(Completable.complete())

    uiEvents.onNext(AddPhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    verify(repository).createPhoneNumberForPatient(patientUuid, newNumber, phoneNumberType = MOBILE, active = true)
  }

  @Test
  @Parameters(method = "validation errors")
  fun `when save is clicked, the number should not be saved if it's invalid`(
      validationError: Result
  ) {
    val newNumber = "123"
    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)
    whenever(repository.createPhoneNumberForPatient(any(), any(), any(), any())).thenReturn(Completable.complete())

    uiEvents.onNext(AddPhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    verify(repository, never()).createPhoneNumberForPatient(any(), any(), any(), any())
  }

  @Test
  @Parameters(method = "validation errors")
  fun `when save is clicked, an error should be shown if it's invalid`(
      validationError: Result
  ) {
    val newNumber = "123"
    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)

    uiEvents.onNext(AddPhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    when (validationError) {
      BLANK, LENGTH_TOO_SHORT -> verify(dialog).showPhoneNumberTooShortError()
      LENGTH_TOO_LONG -> verify(dialog).showPhoneNumberTooLongError()
      VALID -> throw AssertionError()
    }.exhaustive()
  }

  @Suppress("unused")
  private fun `validation errors`() = values().filter { it != VALID }
}
