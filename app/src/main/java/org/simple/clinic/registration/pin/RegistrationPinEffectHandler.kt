package org.simple.clinic.registration.pin

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationPinEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: RegistrationPinUiActions,
    @Assisted private val viewEffectsConsumer: Consumer<RegistrationPinViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        uiActions: RegistrationPinUiActions,
        viewEffectsConsumer: Consumer<RegistrationPinViewEffect>
    ): RegistrationPinEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPinEffect, RegistrationPinEvent> {
    return RxMobius.subtypeEffectHandler<RegistrationPinEffect, RegistrationPinEvent>()
        .addConsumer(ProceedToConfirmPin::class.java, { uiActions.openRegistrationConfirmPinScreen(it.entry) }, schedulers.ui())
        .addConsumer(RegistrationPinViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }
}
