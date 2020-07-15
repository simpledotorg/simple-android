package org.simple.clinic.summary.addphone

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class AddPhoneNumberEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): AddPhoneNumberEffectHandler
  }

  fun build(): ObservableTransformer<AddPhoneNumberEffect, AddPhoneNumberEvent> = RxMobius
      .subtypeEffectHandler<AddPhoneNumberEffect, AddPhoneNumberEvent>()
      .build()
}
