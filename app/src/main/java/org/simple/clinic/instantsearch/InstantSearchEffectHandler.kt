package org.simple.clinic.instantsearch

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class InstantSearchEffectHandler @Inject constructor(
    private val currentFacility: Lazy<Facility>,
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<InstantSearchEffect, InstantSearchEvent> = RxMobius
      .subtypeEffectHandler<InstantSearchEffect, InstantSearchEvent>()
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .build()

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, InstantSearchEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }
}
