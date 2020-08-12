package org.simple.clinic.summary.updatephone

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class UpdatePhoneNumberEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UpdatePhoneNumberUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UpdatePhoneNumberUiActions): UpdatePhoneNumberEffectHandler
  }

  fun build(): ObservableTransformer<UpdatePhoneNumberEffect, UpdatePhoneNumberEvent> = RxMobius
      .subtypeEffectHandler<UpdatePhoneNumberEffect, UpdatePhoneNumberEvent>()
      .build()
}
