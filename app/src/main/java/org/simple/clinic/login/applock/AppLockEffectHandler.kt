package org.simple.clinic.login.applock

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class AppLockEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val schedulersProvider: SchedulersProvider,
    private val lockAfterTimestampValue: MemoryValue<Optional<Instant>>,
    @Assisted private val uiActions: AppLockUiActions
) {

  @AssistedFactory
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
          .map { currentFacility.get() }
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
          .doOnNext { lockAfterTimestampValue.clear() }
          .map { UnlockApp }
    }
  }
}
