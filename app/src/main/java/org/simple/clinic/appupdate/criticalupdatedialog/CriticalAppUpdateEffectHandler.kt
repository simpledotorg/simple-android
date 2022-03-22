package org.simple.clinic.appupdate.criticalupdatedialog

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.appupdate.CheckAppUpdateAvailability
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.Optional

class CriticalAppUpdateEffectHandler @AssistedInject constructor(
    private val appUpdateHelpContact: Lazy<Optional<AppUpdateHelpContact>>,
    private val schedulersProvider: SchedulersProvider,
    private val checkAppUpdateAvailability: CheckAppUpdateAvailability,
    @Assisted private val viewEffectsConsumer: Consumer<CriticalAppUpdateViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<CriticalAppUpdateViewEffect>): CriticalAppUpdateEffectHandler
  }

  fun build(): ObservableTransformer<CriticalAppUpdateEffect, CriticalAppUpdateEvent> {
    return RxMobius
        .subtypeEffectHandler<CriticalAppUpdateEffect, CriticalAppUpdateEvent>()
        .addTransformer(LoadAppUpdateHelpContact::class.java, loadAppUpdateHelpContact())
        .addConsumer(CriticalAppUpdateViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadAppStaleness::class.java, loadAppStaleness())
        .build()
  }

  private fun loadAppUpdateHelpContact(): ObservableTransformer<LoadAppUpdateHelpContact, CriticalAppUpdateEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { appUpdateHelpContact.get() }
          .map(::AppUpdateHelpContactLoaded)
    }
  }

  private fun loadAppStaleness(): ObservableTransformer<LoadAppStaleness, CriticalAppUpdateEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { checkAppUpdateAvailability.loadAppStaleness() }
          .map(::AppStalenessLoaded)
    }
  }
}
