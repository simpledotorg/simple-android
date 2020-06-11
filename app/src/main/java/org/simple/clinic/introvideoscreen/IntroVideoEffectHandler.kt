package org.simple.clinic.introvideoscreen

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class IntroVideoEffectHandler {
  fun build(): ObservableTransformer<IntroVideoEffect, IntroVideoEvent> = RxMobius
      .subtypeEffectHandler<IntroVideoEffect, IntroVideoEvent>()
      .build()
}
