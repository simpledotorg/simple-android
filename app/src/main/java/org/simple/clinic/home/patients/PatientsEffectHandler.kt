package org.simple.clinic.home.patients

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: PatientsUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientsUiActions): PatientsEffectHandler
  }

  fun build(): ObservableTransformer<PatientsEffect, PatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientsEffect, PatientsEvent>()
        .addAction(OpenEnterOtpScreen::class.java, uiActions::openEnterCodeManuallyScreen, schedulers.ui())
        .build()
  }
}
