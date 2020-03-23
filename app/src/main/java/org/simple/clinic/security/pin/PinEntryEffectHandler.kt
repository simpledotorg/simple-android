package org.simple.clinic.security.pin

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.security.PasswordHasher

class PinEntryEffectHandler @AssistedInject constructor(
    private val passwordHasher: PasswordHasher
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(): PinEntryEffectHandler
  }

  fun build(): ObservableTransformer<PinEntryEffect, PinEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<PinEntryEffect, PinEntryEvent>()
        .build()
  }
}
