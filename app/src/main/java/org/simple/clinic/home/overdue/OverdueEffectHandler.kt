package org.simple.clinic.home.overdue

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Provider

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val currentFacilityChanges: Observable<Facility>,
    private val currentFacility: Provider<Facility>,
    private val dataSourceFactory: OverdueAppointmentRowDataSource.Factory.InjectionFactory,
    @Assisted private val uiActions: OverdueUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: OverdueUiActions): OverdueEffectHandler
  }

  fun build(): ObservableTransformer<OverdueEffect, OverdueEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueEffect, OverdueEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addConsumer(LoadOverdueAppointments::class.java, ::loadOverdueAppointments, schedulers.ui())
        .addConsumer(OpenContactPatientScreen::class.java, { uiActions.openPhoneMaskBottomSheet(it.patientUuid) }, schedulers.ui())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid) }, schedulers.ui())
        .build()
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadOverdueAppointments(loadOverdueAppointments: LoadOverdueAppointments) {
    val overdueAppointmentsDataSource = appointmentRepository.overdueAppointmentsDataSource(
        since = loadOverdueAppointments.overdueSince,
        facility = loadOverdueAppointments.facility
    )

    uiActions.showOverdueAppointments(dataSourceFactory.create(loadOverdueAppointments.facility, overdueAppointmentsDataSource))
  }
}
