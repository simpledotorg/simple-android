package org.simple.clinic.introvideoscreen

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class IntroVideoEffectHandler @AssistedInject constructor(
    @Assisted val uiActions: UiActions,
    val schedulersProvider: SchedulersProvider
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): IntroVideoEffectHandler
  }

  fun build(): ObservableTransformer<IntroVideoEffect, IntroVideoEvent> = RxMobius
      .subtypeEffectHandler<IntroVideoEffect, IntroVideoEvent>()
      .addAction(OpenVideo::class.java, { uiActions.openVideo() }, schedulersProvider.ui())
      .build()
}
