package org.simple.clinic.summary.addphone

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.patient.PhoneNumberDetails
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

typealias Ui = AddPhoneNumberDialog
typealias UiChange = (Ui) -> Unit

class AddPhoneNumberDialogController @AssistedInject constructor(
    private val repository: PatientRepository,
    private val validator: PhoneNumberValidator,
    private val uuidGenerator: UuidGenerator,
    @Assisted private val patientUuid: UUID
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: UUID) : AddPhoneNumberDialogController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .compose(ReportAnalyticsEvents())
        .replay()

    return addPhoneNumberToPatient(replayedEvents)
  }

  private fun addPhoneNumberToPatient(events: Observable<UiEvent>): Observable<UiChange> {
    val newNumberAndValidationResult = events
        .ofType<AddPhoneNumberSaveClicked>()
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
        .flatMap { newNumber ->
          repository
              .createPhoneNumberForPatient(
                  uuid = uuidGenerator.v4(),
                  patientUuid = patientUuid,
                  numberDetails = PhoneNumberDetails.mobile(newNumber),
                  active = true
              )
              .andThen(Observable.just({ ui: Ui -> ui.dismiss() }))
        }

    return saveNumber.mergeWith(showValidationError)
  }
}
