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
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.util.PagerFactory
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class OverdueEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val appointmentRepository: AppointmentRepository,
    private val currentFacilityStream: Observable<Facility>,
    private val pagerFactory: PagerFactory,
    private val overdueAppointmentsConfig: OverdueAppointmentsConfig,
    private val overdueDownloadScheduler: OverdueDownloadScheduler,
    private val userClock: UserClock,
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
        .addTransformer(LoadOverdueAppointments_old::class.java, loadOverdueAppointmentsOld())
        .addConsumer(ScheduleDownload::class.java, ::scheduleDownload, schedulers.io())
        .addConsumer(OverdueViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadOverdueAppointments::class.java, loadOverdueAppointments())
        .build()
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
            val today = LocalDate.now(userClock)
            val (overdueAppointmentsWithInYear, overdueAppointmentMoreThanYear) = overdueAppointments
                .partition { ChronoUnit.YEARS.between(it.appointment.scheduledDate, today) == 0L }

            val overdueSections = overdueAppointmentsWithInYear.groupBy { it.callResult?.outcome }

            OverdueAppointmentsLoaded(
                pendingAppointments = overdueSections[null].orEmpty(),
                agreedToVisitAppointments = overdueSections[Outcome.AgreedToVisit].orEmpty(),
                remindToCallLaterAppointments = overdueSections[Outcome.RemindToCallLater].orEmpty(),
                removedFromOverdueAppointments = overdueSections[Outcome.RemovedFromOverdueList].orEmpty(),
                moreThanAnYearOverdueAppointments = overdueAppointmentMoreThanYear
            )
          }
    }
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

  private fun loadOverdueAppointmentsOld(): ObservableTransformer<LoadOverdueAppointments_old, OverdueEvent> {
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
            ).debounce(1, TimeUnit.SECONDS)
          }
          .map(::OverdueAppointmentsLoaded_Old)
    }
  }
}
