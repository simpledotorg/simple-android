package org.simple.clinic.summary.updatephone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber
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
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return Observable.merge(
        preFillExistingNumber(replayedEvents),
        saveUpdatedPhoneNumber(replayedEvents),
        saveExistingPhoneNumber(replayedEvents))
  }

  private fun preFillExistingNumber(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<UpdatePhoneNumberDialogCreated>()
        .flatMap { repository.phoneNumber(it.patientUuid) }
        .unwrapJust()
        .map { { ui: Ui -> ui.preFillPhoneNumber(it.number) } }
  }

  @Suppress("RedundantLambdaArrow")
  private fun saveUpdatedPhoneNumber(events: Observable<UiEvent>): Observable<UiChange> {
    val patientUuidStream = events
        .ofType<UpdatePhoneNumberDialogCreated>()
        .map { it.patientUuid }

    val newNumberAndValidationResult = events
        .ofType<UpdatePhoneNumberSaveClicked>()
        .map { it.number to validator.validate(it.number, type = LANDLINE_OR_MOBILE) }

    val showValidationError = newNumberAndValidationResult
        .map<UiChange> { (_, result) ->
          when (result) {
            is ValidNumber -> { _: Ui -> }
            is Blank, is LengthTooShort -> { ui: Ui -> ui.showPhoneNumberTooShortError() }
            is LengthTooLong -> { ui: Ui -> ui.showPhoneNumberTooLongError() }
          }
        }

    val saveNumber = newNumberAndValidationResult
        .filter { (_, result) -> result == ValidNumber }
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

  /**
   * The dialog is never shown again once it's dismissed, until the phone number
   * is updated again and an appointment is canceled again. In order to identify
   * if the dialog can be shown, the timestamps of the cancelled appointment and
   * the phone number are compared.
   *
   * As a result, it's necessary to always bump the phone number's update
   * timestamp even if it wasn't unchanged.
   */
  private fun saveExistingPhoneNumber(events: Observable<UiEvent>): Observable<UiChange> {
    val cancelClicks = events.ofType<UpdatePhoneNumberCancelClicked>()

    val patientUuidStream = events
        .ofType<UpdatePhoneNumberDialogCreated>()
        .map { it.patientUuid }

    return cancelClicks
        .withLatestFrom(patientUuidStream)
        .flatMap { (_, patientUuid) -> repository.phoneNumber(patientUuid) }
        .unwrapJust()
        .take(1)
        .flatMap { phoneNumber ->
          repository
              .updatePhoneNumberForPatient(phoneNumber.patientUuid, phoneNumber)
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }
  }
}
