package org.simple.clinic.home

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.LocalDate

class HomeScreenEffectHandler @AssistedInject constructor(
    private val currentFacilityStream: Observable<Facility>,
    private val appointmentRepository: AppointmentRepository,
    val patientRepository: PatientRepository,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: HomeScreenUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: HomeScreenUiActions): HomeScreenEffectHandler
  }

  fun build(): ObservableTransformer<HomeScreenEffect, HomeScreenEvent> = RxMobius
      .subtypeEffectHandler<HomeScreenEffect, HomeScreenEvent>()
      .addAction(OpenFacilitySelection::class.java, uiActions::openFacilitySelection, schedulersProvider.ui())
      .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
      .addTransformer(LoadOverdueAppointmentCount::class.java, loadOverdueAppointmentCount())
      .build()

  private fun loadOverdueAppointmentCount(): ObservableTransformer<LoadOverdueAppointmentCount, HomeScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { (facility) ->
            val now = LocalDate.now(userClock)
            appointmentRepository.overdueAppointmentsCount(now, facility)
          }
          .map(::OverdueAppointmentCountLoaded)
    }
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, HomeScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap {
            currentFacilityStream
                .map(::CurrentFacilityLoaded)
          }
    }
  }
}
