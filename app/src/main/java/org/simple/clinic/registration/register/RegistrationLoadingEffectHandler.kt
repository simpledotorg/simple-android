package org.simple.clinic.registration.register

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationLoadingEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Assisted private val uiActions: RegistrationLoadingUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationLoadingUiActions): RegistrationLoadingEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationLoadingEffect, RegistrationLoadingEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationLoadingEffect, RegistrationLoadingEvent>()
        .addTransformer(LoadRegistrationDetails::class.java, loadRegistrationDetails())
        .build()
  }

  private fun loadRegistrationDetails(): ObservableTransformer<LoadRegistrationDetails, RegistrationLoadingEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            val userStream = userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .filterAndUnwrapJust()
                .take(1)

            val facilityStream = userStream
                .switchMap(facilityRepository::currentFacility)
                .subscribeOn(schedulers.io())
                .take(1)

            Observables.zip(userStream, facilityStream) { user, facility -> RegistrationDetailsLoaded(user, facility) }
          }
    }
  }
}
