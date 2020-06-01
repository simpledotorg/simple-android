package org.simple.clinic.summary.addphone

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PhoneNumberDetails
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
import org.simple.clinic.uuid.FakeUuidGenerator
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
  private val generatedPhoneUuid = UUID.fromString("f94bd99b-b182-4138-8e77-d91908b7ada5")

  private lateinit var controller: AddPhoneNumberDialogController

  @Before
  fun setup() {
    controller = AddPhoneNumberDialogController(repository, validator, FakeUuidGenerator.fixed(generatedPhoneUuid))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(dialog) }
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    val newNumber = "1234567890"
    val numberDetails = PhoneNumberDetails.mobile(newNumber)

    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(VALID)
    whenever(repository.createPhoneNumberForPatient(
        uuid = generatedPhoneUuid,
        patientUuid = patientUuid,
        numberDetails = numberDetails,
        active = true
    )).thenReturn(Completable.complete())

    uiEvents.onNext(AddPhoneNumberDialogCreated(patientUuid))
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    verify(repository).createPhoneNumberForPatient(
        uuid = generatedPhoneUuid,
        patientUuid = patientUuid,
        numberDetails = numberDetails,
        active = true
    )
  }

  @Test
  @Parameters(method = "validation errors")
  fun `when save is clicked, the number should not be saved if it's invalid`(
      validationError: Result
  ) {
    val newNumber = "123"
    whenever(validator.validate(newNumber, type = LANDLINE_OR_MOBILE)).thenReturn(validationError)
    whenever(repository.createPhoneNumberForPatient(
        uuid = generatedPhoneUuid,
        patientUuid = patientUuid,
        numberDetails = PhoneNumberDetails.mobile(newNumber),
        active = true
    )).thenReturn(Completable.complete())

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
