package org.simple.clinic.login.applock

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class AppLockEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
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
      .build()
}
