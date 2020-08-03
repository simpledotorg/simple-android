package org.simple.clinic.forgotpin.confirmpin

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.clearpatientdata.SyncAndClearPatientData
import org.simple.clinic.user.resetpin.ResetPinResult.NetworkError
import org.simple.clinic.user.resetpin.ResetPinResult.Success
import org.simple.clinic.user.resetpin.ResetPinResult.UnexpectedError
import org.simple.clinic.user.resetpin.ResetPinResult.UserNotFound
import org.simple.clinic.user.resetpin.ResetUserPin
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.widgets.UiEvent

typealias Ui = ForgotPinConfirmPinUi
typealias UiChange = (Ui) -> Unit

class ForgotPinConfirmPinScreenController @AssistedInject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val resetUserPin: ResetUserPin,
    private val syncAndClearPatientData: SyncAndClearPatientData,
    @Assisted private val previousPin: String
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(previousPin: String): ForgotPinConfirmPinScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
