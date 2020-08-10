package org.simple.clinic.signature

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class SignatureEffectHandler @AssistedInject constructor(
    @Assisted private val ui: SignatureUiActions,
    private val schedulersProvider: SchedulersProvider
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(ui: SignatureUiActions): SignatureEffectHandler
  }

  fun build(): ObservableTransformer<SignatureEffect,
      SignatureEvent> = RxMobius
      .subtypeEffectHandler<SignatureEffect, SignatureEvent>()
      .addAction(ClearSignature::class.java, ui::clearSignature, schedulersProvider.ui())
      .build()
}
