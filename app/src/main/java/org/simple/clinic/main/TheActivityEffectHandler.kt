package org.simple.clinic.main

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.navigation.v2.History
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterTrue
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import java.util.Optional

class TheActivityEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    private val patientRepository: PatientRepository,
    private val lockAfterTimestamp: MemoryValue<Optional<Instant>>,
    @Assisted private val uiActions: TheActivityUiActions,
    @Assisted private val provideCurrentScreenHistory: () -> History
) {

  @AssistedFactory
  interface InjectionFactory {
    fun create(
        uiActions: TheActivityUiActions,
        provideCurrentScreenHistory: () -> History
    ): TheActivityEffectHandler
  }

  fun build(): ObservableTransformer<TheActivityEffect, TheActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<TheActivityEffect, TheActivityEvent>()
        .addTransformer(LoadInitialScreenInfo::class.java, loadInitialScreenInfo())
        .addAction(ClearLockAfterTimestamp::class.java, lockAfterTimestamp::clear)
        .addTransformer(ListenForUserVerifications::class.java, listenForUserVerifications())
        .addAction(ShowUserLoggedOutOnOtherDeviceAlert::class.java, uiActions::showUserLoggedOutOnOtherDeviceAlert, schedulers.ui())
        .addTransformer(ListenForUserUnauthorizations::class.java, listenForUserUnauthorizations())
        .addAction(RedirectToLoginScreen::class.java, uiActions::redirectToLogin, schedulers.ui())
        .addTransformer(ListenForUserDisapprovals::class.java, listenForUserDisapprovals())
        .addTransformer(ClearPatientData::class.java, clearPatientData())
        .addTransformer(ShowAccessDeniedScreen::class.java, openAccessDeniedScreen())
        .addConsumer(SetCurrentScreenHistory::class.java, { uiActions.setCurrentScreenHistory(it.history) }, schedulers.ui())
        .build()
  }

  private fun loadInitialScreenInfo(): ObservableTransformer<LoadInitialScreenInfo, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { userSession.loggedInUserImmediate()!! }
          .map {
            InitialScreenInfoLoaded(
                user = it,
                currentTimestamp = Instant.now(utcClock),
                lockAtTimestamp = lockAfterTimestamp.get(),
                currentHistory = provideCurrentScreenHistory()
            )
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
