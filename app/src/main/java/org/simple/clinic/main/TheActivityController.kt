package org.simple.clinic.main

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterTrue
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = TheActivityUi
typealias UiChange = (Ui) -> Unit

class TheActivityController @Inject constructor(
    private val userSession: UserSession,
    private val appLockConfig: AppLockConfig,
    private val patientRepository: PatientRepository,
    private val utcClock: UtcClock,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
        displayUserLoggedOutOnOtherDevice(replayedEvents),
        redirectToLoginScreen(),
        redirectToAccessDeniedScreen()
    )
  }

  private fun displayUserLoggedOutOnOtherDevice(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<LifecycleEvent.ActivityStarted>()
        .flatMap { userSession.loggedInUser() }
        .compose(NewlyVerifiedUser())
        .map { { ui: Ui -> ui.showUserLoggedOutOnOtherDeviceAlert() } }
  }

  private fun redirectToLoginScreen(): Observable<UiChange> {
    return userSession
        .isUserUnauthorized()
        .distinctUntilChanged()
        .filter { isUserUnauthorized -> isUserUnauthorized }
        .map { Ui::redirectToLogin }
  }

  private fun redirectToAccessDeniedScreen(): Observable<UiChange> {
    return userSession
        .isUserDisapproved()
        .filterTrue()
        .flatMap {
          val fullName = userSession.loggedInUserImmediate()?.fullName
          patientRepository.clearPatientData()
              .andThen(Observable.just { ui: Ui -> ui.showAccessDeniedScreen(fullName!!) })
        }
  }
}
