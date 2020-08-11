package org.simple.clinic.summary.updatephone

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.unwrapJust
import org.simple.clinic.widgets.UiEvent

typealias Ui = UpdatePhoneNumberDialogUi
typealias UiChange = (Ui) -> Unit

class UpdatePhoneNumberDialogController @AssistedInject constructor(
    private val repository: PatientRepository,
    private val validator: PhoneNumberValidator,
    @Assisted private val patientUuid: PatientUuid
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(patientUuid: PatientUuid): UpdatePhoneNumberDialogController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return saveExistingPhoneNumber(replayedEvents)
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
    return events
        .ofType<UpdatePhoneNumberCancelClicked>()
        .flatMap { repository.phoneNumber(patientUuid) }
        .unwrapJust()
        .take(1)
        .flatMap { phoneNumber ->
          repository
              .updatePhoneNumberForPatient(phoneNumber.patientUuid, phoneNumber)
              .andThen(Observable.just { ui: Ui -> ui.closeDialog() })
        }
  }
}
