package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.LockAtTime
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class AppLockEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
    private val facilityRepository: FacilityRepository,
    private val schedulersProvider: SchedulersProvider,
    @TypedPreference(LockAtTime) private val lockAfterTimestamp: Preference<Instant>,
    @Assisted private val uiActions: AppLockUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: AppLockUiActions): AppLockEffectHandler
  }

  fun build(): ObservableTransformer<AppLockEffect, AppLockEvent> = RxMobius
      .subtypeEffectHandler<AppLockEffect, AppLockEvent>()
      .addAction(ExitApp::class.java, uiActions::exitApp, schedulersProvider.ui())
      .addAction(ShowConfirmResetPinDialog::class.java, uiActions::showConfirmResetPinDialog, schedulersProvider.ui())
      .addAction(RestorePreviousScreen::class.java, uiActions::restorePreviousScreen, schedulersProvider.ui())
      .addTransformer(UnlockOnAuthentication::class.java, unlockOnAuthentication())
      .addTransformer(LoadLoggedInUser::class.java, loadLoggedInUser())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .build()

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, AppLockEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentUser.get() }
          .switchMap { facilityRepository.currentFacility(it) }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadLoggedInUser(): ObservableTransformer<LoadLoggedInUser, AppLockEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentUser.get() }
          .map(::LoggedInUserLoaded)
    }
  }

  private fun unlockOnAuthentication(): ObservableTransformer<UnlockOnAuthentication, AppLockEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext {
            lockAfterTimestamp.delete()
          }
          .map { UnlockApp }
    }
  }
}
