package org.simple.clinic.facility.change

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilityChangeEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val currentFacility: Lazy<Facility>,
    @Assisted private val uiActions: FacilityChangeUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: FacilityChangeUiActions): FacilityChangeEffectHandler
  }

  fun build(): ObservableTransformer<FacilityChangeEffect, FacilityChangeEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilityChangeEffect, FacilityChangeEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addConsumer(OpenConfirmFacilityChangeSheet::class.java, { uiActions.openConfirmationSheet(it.facility) }, schedulers.ui())
        .addAction(GoBack::class.java, uiActions::goBack, schedulers.ui())
        .build()
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, FacilityChangeEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { CurrentFacilityLoaded(currentFacility.get()) }
    }
  }
}
