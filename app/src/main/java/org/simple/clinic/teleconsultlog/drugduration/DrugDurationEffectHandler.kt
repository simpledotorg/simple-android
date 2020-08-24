package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class DrugDurationEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: DrugDurationUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: DrugDurationUiActions): DrugDurationEffectHandler
  }

  fun build(): ObservableTransformer<DrugDurationEffect, DrugDurationEvent> = RxMobius
      .subtypeEffectHandler<DrugDurationEffect, DrugDurationEvent>()
      .addConsumer(SaveDrugDuration::class.java, { uiActions.saveDrugDuration(it.duration) }, schedulersProvider.ui())
      .build()
}
