package org.simple.clinic.recentpatientsview

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class LatestRecentPatientsEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    @Assisted private val uiActions: LatestRecentPatientsUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: LatestRecentPatientsUiActions): LatestRecentPatientsEffectHandler
  }

  fun build(): ObservableTransformer<LatestRecentPatientsEffect, LatestRecentPatientsEvent> {
    return RxMobius
        .subtypeEffectHandler<LatestRecentPatientsEffect, LatestRecentPatientsEvent>()
        .build()
  }
}
