package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LockAtTime
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import javax.inject.Inject

typealias Ui = AppLockScreenUi
typealias UiChange = (Ui) -> Unit

class AppLockScreenController @Inject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @TypedPreference(LockAtTime) private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
