package org.simple.clinic.appupdate.criticalupdatedialog

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.Optional
import javax.inject.Inject

class CriticalAppUpdateEffectHandler @Inject constructor(
    private val appUpdateHelpContact: Lazy<Optional<AppUpdateHelpContact>>,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<CriticalAppUpdateEffect, CriticalAppUpdateEvent> {
    return RxMobius
        .subtypeEffectHandler<CriticalAppUpdateEffect, CriticalAppUpdateEvent>()
        .addTransformer(LoadAppUpdateHelpContact::class.java, loadAppUpdateHelpContact())
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
}
