package org.simple.clinic.home.overdue

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val appointmentRepository: AppointmentRepository,
    @Assisted private val uiActions: OverdueUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: OverdueUiActions): OverdueEffectHandler
  }

  fun build(): ObservableTransformer<OverdueEffect, OverdueEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueEffect, OverdueEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addTransformer(LoadOverdueAppointments::class.java, loadOverdueAppointments())
        .build()
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .filterAndUnwrapJust()
                .take(1)
                .switchMap(facilityRepository::currentFacility)
                .map(::CurrentFacilityLoaded)
          }
    }
  }

  private fun loadOverdueAppointments(): ObservableTransformer<LoadOverdueAppointments, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            appointmentRepository
                .overdueAppointments(since = effect.overdueSince, facility = effect.facility)
                .subscribeOn(schedulers.io())
                .map(::OverdueAppointmentsLoaded)
          }
    }
  }
}
