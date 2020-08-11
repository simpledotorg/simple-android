package org.simple.clinic.summary.updatephone

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.registration.phone.PhoneNumberValidator
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

    return Observable.never()
  }
}
