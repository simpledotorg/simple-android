package org.simple.clinic.recentpatient

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class AllRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: AllRecentPatientsUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: AllRecentPatientsUiActions): AllRecentPatientsEffectHandler
  }

  fun build(): ObservableTransformer<AllRecentPatientsEffect, AllRecentPatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<AllRecentPatientsEffect, AllRecentPatientsEvent>()
        .build()
  }
}
