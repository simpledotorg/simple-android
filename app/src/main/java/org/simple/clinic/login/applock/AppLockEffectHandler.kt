package org.simple.clinic.login.applock

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import java.util.Optional

class AppLockEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val schedulersProvider: SchedulersProvider,
    private val lockAfterTimestampValue: MemoryValue<Optional<Instant>>,
    @Assisted private val viewEffectsConsumer: Consumer<AppLockViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<AppLockViewEffect>): AppLockEffectHandler
  }

  fun build(): ObservableTransformer<AppLockEffect, AppLockEvent> = RxMobius
      .subtypeEffectHandler<AppLockEffect, AppLockEvent>()
      .addTransformer(UnlockOnAuthentication::class.java, unlockOnAuthentication())
      .addTransformer(LoadLoggedInUser::class.java, loadLoggedInUser())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addConsumer(AppLockViewEffect::class.java, viewEffectsConsumer::accept)
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
