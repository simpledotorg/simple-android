package org.simple.clinic.home.overdue

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val currentFacility: Lazy<Facility>,
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
        .addConsumer(OpenContactPatientScreen::class.java, { uiActions.openPhoneMaskBottomSheet(it.patientUuid)}, schedulers.ui())
        .build()
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { CurrentFacilityLoaded(currentFacility.get()) }
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
