package org.simple.clinic.home.overdue

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.overdue.OverdueAppointmentSelector
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val currentFacilityStream: Observable<Facility>,
    private val pagerFactory: PagerFactory,
    private val overdueAppointmentsConfig: OverdueAppointmentsConfig,
    private val overdueDownloadScheduler: OverdueDownloadScheduler,
    private val userClock: UserClock,
    private val overdueAppointmentSelector: OverdueAppointmentSelector,
    @Assisted private val viewEffectsConsumer: Consumer<OverdueViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<OverdueViewEffect>
    ): OverdueEffectHandler
  }

  fun build(): ObservableTransformer<OverdueEffect, OverdueEvent> {
    return RxMobius
        .subtypeEffectHandler<OverdueEffect, OverdueEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addConsumer(ScheduleDownload::class.java, ::scheduleDownload, schedulers.io())
        .addConsumer(OverdueViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadOverdueAppointments::class.java, loadOverdueAppointments())
        .addConsumer(ToggleOverdueAppointmentSelection::class.java, ::toggleOverdueAppointmentSelection, schedulers.io())
        .addTransformer(LoadSelectedOverdueAppointmentIds::class.java, loadSelectedOverdueAppointmentIds())
        .addConsumer(ClearSelectedOverdueAppointments::class.java, ::clearSelectedOverdueAppointments)
        .build()
  }

  private fun clearSelectedOverdueAppointments(effect: ClearSelectedOverdueAppointments) {
    overdueAppointmentSelector.clearSelection()
  }

  private fun loadSelectedOverdueAppointmentIds(): ObservableTransformer<LoadSelectedOverdueAppointmentIds, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.computation())
          .switchMap { overdueAppointmentSelector.selectedAppointmentIdsStream }
          .map(::SelectedOverdueAppointmentsLoaded)
    }
  }

  private fun toggleOverdueAppointmentSelection(effect: ToggleOverdueAppointmentSelection) {
    overdueAppointmentSelector.toggleSelection(effect.appointmentId)
  }

  private fun loadOverdueAppointments(): ObservableTransformer<LoadOverdueAppointments, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { (overdueSince, facility) ->
            appointmentRepository.overdueAppointmentsInFacilityNew(
                since = overdueSince,
                facilityId = facility.uuid
            )
          }
          .map { overdueAppointments ->
            val (overdueAppointmentsWithInYear, overdueAppointmentMoreThanYear) = partitionAppointmentsIntoWithInAnYearAndMoreThanAnYear(
                overdueAppointments = overdueAppointments
            )
            val overdueSections = overdueAppointmentsWithInYear.groupBy { it.callResult?.outcome }
            val overdueAppointmentSections = OverdueAppointmentSections(
                pendingAppointments = overdueSections[null].orEmpty(),
                agreedToVisitAppointments = overdueSections[Outcome.AgreedToVisit].orEmpty(),
                remindToCallLaterAppointments = overdueSections[Outcome.RemindToCallLater].orEmpty(),
                removedFromOverdueAppointments = overdueSections[Outcome.RemovedFromOverdueList].orEmpty(),
                moreThanAnYearOverdueAppointments = overdueAppointmentMoreThanYear
            )

            OverdueAppointmentsLoaded(overdueAppointmentSections = overdueAppointmentSections)
          }
    }
  }

  private fun partitionAppointmentsIntoWithInAnYearAndMoreThanAnYear(
      overdueAppointments: List<OverdueAppointment>
  ): Pair<List<OverdueAppointment>, List<OverdueAppointment>> {
    val today = LocalDate.now(userClock)

    return overdueAppointments
        .partition { ChronoUnit.YEARS.between(it.appointment.scheduledDate, today) == 0L }
  }

  private fun scheduleDownload(effect: ScheduleDownload) {
    overdueDownloadScheduler.schedule(effect.fileFormat)
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, OverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .switchMap { currentFacilityStream }
          .map(::CurrentFacilityLoaded)
    }
  }
}
