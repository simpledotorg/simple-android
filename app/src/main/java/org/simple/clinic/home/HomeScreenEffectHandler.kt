package org.simple.clinic.home

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class HomeScreenEffectHandler @AssistedInject constructor(
    private val currentFacilityStream: Observable<Facility>,
    val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<HomeScreenViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<HomeScreenViewEffect>): HomeScreenEffectHandler
  }

  fun build(): ObservableTransformer<HomeScreenEffect, HomeScreenEvent> = RxMobius
      .subtypeEffectHandler<HomeScreenEffect, HomeScreenEvent>()
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addConsumer(HomeScreenViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, HomeScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap {
            currentFacilityStream
                .map(::CurrentFacilityLoaded)
          }
    }
  }
}
