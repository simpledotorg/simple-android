package org.simple.clinic.teleconsultlog.drugduration

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class DrugDurationEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: DrugDurationUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: DrugDurationUiActions): DrugDurationEffectHandler
  }

  fun build(): ObservableTransformer<DrugDurationEffect, DrugDurationEvent> = RxMobius
      .subtypeEffectHandler<DrugDurationEffect, DrugDurationEvent>()
      .addConsumer(SaveDrugDuration::class.java, { uiActions.saveDrugDuration(it.duration) }, schedulersProvider.ui())
      .addConsumer(PrefillDrugDuration::class.java, { uiActions.prefillDrugDuration(it.duration) }, schedulersProvider.ui())
      .build()
}
