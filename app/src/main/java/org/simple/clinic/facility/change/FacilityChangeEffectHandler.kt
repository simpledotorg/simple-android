package org.simple.clinic.facility.change

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class FacilityChangeEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Assisted private val uiActions: FacilityChangeUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: FacilityChangeUiActions): FacilityChangeEffectHandler
  }

  fun build(): ObservableTransformer<FacilityChangeEffect, FacilityChangeEvent> {
    return RxMobius
        .subtypeEffectHandler<FacilityChangeEffect, FacilityChangeEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addConsumer(OpenConfirmFacilityChangeSheet::class.java, { uiActions.openConfirmationSheet(it.facility)}, schedulers.ui())
        .build()
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, FacilityChangeEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .filterAndUnwrapJust()
                .switchMap(facilityRepository::currentFacility)
                .take(1)
                .map(::CurrentFacilityLoaded)
          }
    }
  }
}
