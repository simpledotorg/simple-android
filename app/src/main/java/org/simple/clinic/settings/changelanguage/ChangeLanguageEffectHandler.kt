package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

object ChangeLanguageEffectHandler {

  fun create(schedulersProvider: SchedulersProvider): ObservableTransformer<ChangeLanguageEffect, ChangeLanguageEvent> {
    return RxMobius
        .subtypeEffectHandler<ChangeLanguageEffect, ChangeLanguageEvent>()
        .build()
  }
}
