package org.simple.clinic.home.overdue

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.analytics.NetworkCapabilitiesProvider
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.scheduler.SchedulersProvider

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val currentFacilityStream: Observable<Facility>,
    private val pagerFactory: PagerFactory,
    private val overdueAppointmentsConfig: OverdueAppointmentsConfig,
    private val networkCapabilitiesProvider: NetworkCapabilitiesProvider,
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
        .addTransformer(LoadOverdueAppointments::class.java, loadOverdueAppointments())
        .addConsumer(OpenContactPatientScreen::class.java, { uiActions.openPhoneMaskBottomSheet(it.patientUuid) }, schedulers.ui())
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid) }, schedulers.ui())
        .addConsumer(ShowOverdueAppointments::class.java, ::showOverdueAppointments, schedulers.ui())
        .addTransformer(LoadNetworkConnectivityStatus::class.java, checkIfNetworkIsConnected())
        .addAction(ShowNoActiveNetworkConnectionDialog::class.java, uiActions::showNoActiveNetworkConnectionDialog, schedulers.ui())
        .build()
  }

  private fun checkIfNetworkIsConnected(): ObservableTransformer<LoadNetworkConnectivityStatus, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { networkCapabilitiesProvider.networkConnectivityStatus() }
          .map(::NetworkConnectivityStatusLoaded)
    }
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
          .switchMap { currentFacilityStream }
          .map(::CurrentFacilityLoaded)
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
                pageSize = overdueAppointmentsConfig.overdueAppointmentsLoadSize,
                enablePlaceholders = true
            )
          }
          .map(::OverdueAppointmentsLoaded)
    }
  }
}
