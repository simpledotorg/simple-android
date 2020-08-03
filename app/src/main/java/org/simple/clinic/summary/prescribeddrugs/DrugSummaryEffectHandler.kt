package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class DrugSummaryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: DrugSummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: DrugSummaryUiActions): DrugSummaryEffectHandler
  }

  fun build(): ObservableTransformer<DrugSummaryEffect, DrugSummaryEvent> = RxMobius
      .subtypeEffectHandler<DrugSummaryEffect, DrugSummaryEvent>()
      .build()
}
