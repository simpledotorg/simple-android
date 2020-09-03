package org.simple.clinic.main

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.main.TypedPreference.Type.LockAtTime
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterTrue
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class TheActivityEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    private val patientRepository: PatientRepository,
    @TypedPreference(LockAtTime) private val lockAfterTimestamp: Preference<Instant>,
    private val lockAfterTimestampValue: MemoryValue<Instant>,
    @Assisted private val uiActions: TheActivityUiActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(uiActions: TheActivityUiActions): TheActivityEffectHandler
  }

  fun build(): ObservableTransformer<TheActivityEffect, TheActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<TheActivityEffect, TheActivityEvent>()
        .addTransformer(LoadAppLockInfo::class.java, loadShowAppLockInto())
        .addAction(ClearLockAfterTimestamp::class.java, { lockAfterTimestamp.delete() }, schedulers.io())
        .addAction(ShowAppLockScreen::class.java, uiActions::showAppLockScreen, schedulers.ui())
        .addTransformer(UpdateLockTimestamp::class.java, updateAppLockTime())
        .addTransformer(ListenForUserVerifications::class.java, listenForUserVerifications())
        .addAction(ShowUserLoggedOutOnOtherDeviceAlert::class.java, uiActions::showUserLoggedOutOnOtherDeviceAlert, schedulers.ui())
        .addTransformer(ListenForUserUnauthorizations::class.java, listenForUserUnauthorizations())
        .addAction(RedirectToLoginScreen::class.java, uiActions::redirectToLogin, schedulers.ui())
        .addTransformer(ListenForUserDisapprovals::class.java, listenForUserDisapprovals())
        .addTransformer(ClearPatientData::class.java, clearPatientData())
        .addTransformer(ShowAccessDeniedScreen::class.java, openAccessDeniedScreen())
        .build()
  }

  private fun loadShowAppLockInto(): ObservableTransformer<LoadAppLockInfo, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .map {
                  AppLockInfoLoaded(
                      user = it,
                      currentTimestamp = Instant.now(utcClock),
                      lockAtTimestamp = lockAfterTimestamp.get()
                  )
                }
          }
    }
  }

  private fun updateAppLockTime(): ObservableTransformer<UpdateLockTimestamp, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { effect ->
            val shouldUpdateLockTimestamp = userSession.isUserLoggedIn() && !lockAfterTimestamp.isSet

            if (shouldUpdateLockTimestamp) {
              lockAfterTimestamp.set(effect.lockAt)
            }

            Observable.empty<TheActivityEvent>()
          }
    }
  }

  private fun listenForUserVerifications(): ObservableTransformer<ListenForUserVerifications, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .compose(NewlyVerifiedUser())
                .map { UserWasJustVerified }
          }
    }
  }

  private fun listenForUserUnauthorizations(): ObservableTransformer<ListenForUserUnauthorizations, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .isUserUnauthorized()
                .subscribeOn(schedulers.io())
                .distinctUntilChanged()
                .filterTrue()
                .map { UserWasUnauthorized }
          }
    }
  }

  private fun listenForUserDisapprovals(): ObservableTransformer<ListenForUserDisapprovals, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .isUserDisapproved()
                .subscribeOn(schedulers.io())
                .filterTrue()
                .map { UserWasDisapproved }
          }
    }
  }

  private fun clearPatientData(): ObservableTransformer<ClearPatientData, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            patientRepository
                .clearPatientData()
                .subscribeOn(schedulers.io())
                .andThen(Observable.just(PatientDataCleared))
          }
    }
  }

  private fun openAccessDeniedScreen(): ObservableTransformer<ShowAccessDeniedScreen, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { userSession.loggedInUserImmediate()!!.fullName }
          .observeOn(schedulers.ui())
          .doOnNext(uiActions::showAccessDeniedScreen)
          .flatMap { Observable.empty<TheActivityEvent>() }
    }
  }
}
