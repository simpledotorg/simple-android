package org.simple.clinic.home

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class HomeScreenEffectHandler @AssistedInject constructor(
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: HomeScreenUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: HomeScreenUiActions): HomeScreenEffectHandler
  }

  fun build(): ObservableTransformer<HomeScreenEffect, HomeScreenEvent> = RxMobius
      .subtypeEffectHandler<HomeScreenEffect, HomeScreenEvent>()
      .addAction(OpenFacilitySelection::class.java, uiActions::openFacilitySelection, schedulersProvider.ui())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .build()

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, HomeScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { userSession.requireLoggedInUser() }
          .flatMap { facilityRepository.currentFacility(it) }
          .map(::CurrentFacilityLoaded)
    }
  }
}
