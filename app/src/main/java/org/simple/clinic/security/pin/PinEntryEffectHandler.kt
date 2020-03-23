package org.simple.clinic.security.pin

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer

class PinEntryEffectHandler {

  fun build(): ObservableTransformer<PinEntryEffect, PinEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<PinEntryEffect, PinEntryEvent>()
        .build()
  }
}
