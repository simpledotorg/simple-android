package org.simple.clinic.signature

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class SignatureEffectHandler {

  fun build(): ObservableTransformer<SignatureEffect,
      SignatureEvent> = RxMobius
      .subtypeEffectHandler<SignatureEffect, SignatureEvent>()
      .build()
}
