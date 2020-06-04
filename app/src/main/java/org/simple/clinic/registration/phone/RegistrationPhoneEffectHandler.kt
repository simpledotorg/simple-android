package org.simple.clinic.registration.phone

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationPhoneEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: RegistrationPhoneUiActions,
    private val schedulers: SchedulersProvider
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationPhoneUiActions): RegistrationPhoneEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPhoneEffect, RegistrationPhoneEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationPhoneEffect, RegistrationPhoneEvent>()
        .addConsumer(PrefillFields::class.java, { uiActions.preFillUserDetails(it.entry) }, schedulers.ui())
        .build()
  }
}
