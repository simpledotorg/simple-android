package org.simple.clinic.introvideoscreen

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class IntroVideoEffectHandler @AssistedInject constructor(
    @Assisted val uiActions: UiActions,
    val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<IntroVideoViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions, viewEffectsConsumer: Consumer<IntroVideoViewEffect>): IntroVideoEffectHandler
  }

  fun build(): ObservableTransformer<IntroVideoEffect, IntroVideoEvent> = RxMobius
      .subtypeEffectHandler<IntroVideoEffect, IntroVideoEvent>()
      .addAction(OpenVideo::class.java, { uiActions.openVideo() }, schedulersProvider.ui())
      .addAction(OpenHome::class.java, { uiActions.openHome() }, schedulersProvider.ui())
      .addConsumer(IntroVideoViewEffect::class.java, viewEffectsConsumer::accept)
      .build()
}
