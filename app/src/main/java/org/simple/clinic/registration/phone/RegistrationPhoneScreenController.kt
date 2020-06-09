package org.simple.clinic.registration.phone

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.finduser.UserLookup
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = RegistrationPhoneUi
typealias UiChange = (Ui) -> Unit

class RegistrationPhoneScreenController @Inject constructor(
    private val userSession: UserSession,
    private val userLookup: UserLookup,
    private val numberValidator: PhoneNumberValidator,
    private val facilitySync: FacilitySync,
    private val uuidGenerator: UuidGenerator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
