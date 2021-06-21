package org.simple.clinic.home.overdue

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Provider

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val currentFacility: Provider<Facility>,
    private val pagerFactory: PagerFactory,
    private val overdueAppointmentsConfig: OverdueAppointmentsConfig,
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
        .addTransformer(LoadOverdueAppointments_old::class.java, loadOverdueAppointments_old())
        .addTransformer(LoadOverdueAppointments::class.java, loadOverdueAppointments())
        .addConsumer(OpenContactPatientScreen::class.java, { uiActions.openPhoneMaskBottomSheet(it.patientUuid) }, schedulers.ui())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid) }, schedulers.ui())
        .addConsumer(ShowOverdueAppointments::class.java, ::showOverdueAppointments, schedulers.ui())
        .build()
  }

  private fun showOverdueAppointments(effect: ShowOverdueAppointments) {
    uiActions.showOverdueAppointments(
        overdueAppointments = effect.overdueAppointments,
        isDiabetesManagementEnabled = effect.isDiabetesManagementEnabled)
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadOverdueAppointments_old(): ObservableTransformer<LoadOverdueAppointments_old, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { (overdueSince, facility) ->
            pagerFactory.createPager(
                sourceFactory = {
                  appointmentRepository.overdueAppointmentsInFacility_old(
                      since = overdueSince,
                      facilityId = facility.uuid
                  )
                },
                pageSize = overdueAppointmentsConfig.overdueAppointmentsLoadSize
            )
          }
          .map { pagingData ->
            OverdueAppointmentsLoaded(pagingData)
          }
    }
  }

  private fun loadOverdueAppointments(): ObservableTransformer<LoadOverdueAppointments, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { (overdueSince, facility) ->
            pagerFactory.createPager(
                sourceFactory = {
                  appointmentRepository.overdueAppointmentsInFacility(
                      since = overdueSince,
                      facilityId = facility.uuid
                  )
                },
                pageSize = overdueAppointmentsConfig.overdueAppointmentsLoadSize
            )
          }
          .map(::OverdueAppointmentsLoaded)
    }
  }
}
