package org.simple.clinic.summary.updatephone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = UpdatePhoneNumberDialog
typealias UiChange = (Ui) -> Unit

class UpdatePhoneNumberDialogController @Inject constructor(
    private val repository: PatientRepository,
    private val validator: PhoneNumberValidator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents())
        .replay(1)
        .refCount()

    return Observable.merge(
        preFillExistingNumber(replayedEvents),
        savePhoneNumber(replayedEvents))
  }

  private fun preFillExistingNumber(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<UpdatePhoneNumberDialogCreated>()
        .flatMap { repository.phoneNumber(it.patientUuid) }
        .unwrapJust()
        .map { { ui: Ui -> ui.preFillPhoneNumber(it.number) } }
  }

  @Suppress("RedundantLambdaArrow")
  private fun savePhoneNumber(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuidStream = events
        .ofType<UpdatePhoneNumberDialogCreated>()
        .map { it.patientUuid }

    val newNumberAndValidationResult = events
        .ofType<UpdatePhoneNumberSaveClicked>()
        .map { it.number to validator.validate(it.number, type = LANDLINE_OR_MOBILE) }

    val showValidationError = newNumberAndValidationResult
        .map<UiChange> { (_, result) ->
          when (result) {
            VALID -> { _: Ui -> }
            BLANK, LENGTH_TOO_SHORT -> { ui: Ui -> ui.showPhoneNumberTooShortError() }
            LENGTH_TOO_LONG -> { ui: Ui -> ui.showPhoneNumberTooLongError() }
          }
        }

    val saveNumber = newNumberAndValidationResult
        .filter { (_, result) -> result == VALID }
        .map { (newNumber, _) -> newNumber }
        .withLatestFrom(patientUuidStream)
        .flatMap { (newNumber, patientUuid) ->
          repository.phoneNumber(patientUuid)
              .unwrapJust()
              .take(1)
              .flatMapCompletable { existingPhone ->
                repository.updatePhoneNumberForPatient(
                    patientUuid = patientUuid,
                    phoneNumber = existingPhone.copy(number = newNumber)
                )
              }
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }

    return saveNumber.mergeWith(showValidationError)
  }
}
