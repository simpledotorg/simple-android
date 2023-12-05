package org.simple.clinic.login.applock

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.DataProtectionConsent
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import java.util.Optional

class AppLockEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    @TypedPreference(DataProtectionConsent) private val hasUserConsentedToDataProtectionPreference: Preference<Boolean>,
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
      .addTransformer(LoadDataProtectionConsent::class.java, loadDataProtectionConsent())
      .addTransformer(MarkDataProtectionConsent::class.java, markDataProtectionConsent())
      .build()

  private fun markDataProtectionConsent(): ObservableTransformer<MarkDataProtectionConsent, AppLockEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { hasUserConsentedToDataProtectionPreference.set(true) }
          .map { FinishedMarkingDataProtectionConsent }
    }
  }

  private fun loadDataProtectionConsent(): ObservableTransformer<LoadDataProtectionConsent, AppLockEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { hasUserConsentedToDataProtectionPreference.get() }
          .map { DataProtectionConsentLoaded(hasUserConsentedToDataProtection = it) }
    }
  }

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
