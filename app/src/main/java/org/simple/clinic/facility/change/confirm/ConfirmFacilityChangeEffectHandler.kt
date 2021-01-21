package org.simple.clinic.facility.change.confirm

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.FacilitySyncGroupSwitchedAt
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.reports.ReportsSync
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import javax.inject.Named

class ConfirmFacilityChangeEffectHandler @AssistedInject constructor(
    private val facilityRepository: FacilityRepository,
    private val reportsRepository: ReportsRepository,
    private val reportsSync: ReportsSync,
    private val schedulersProvider: SchedulersProvider,
    private val clock: UtcClock,
    @Assisted private val uiActions: ConfirmFacilityChangeUiActions,
    @Named("is_facility_switched") private val isFacilitySwitchedPreference: Preference<Boolean>,
    @TypedPreference(FacilitySyncGroupSwitchedAt) private val facilitySyncGroupSwitchAtPreference: Preference<Optional<Instant>>
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ConfirmFacilityChangeUiActions): ConfirmFacilityChangeEffectHandler
  }

  fun build(): ObservableTransformer<ConfirmFacilityChangeEffect, ConfirmFacilityChangeEvent> {
    return RxMobius
        .subtypeEffectHandler<ConfirmFacilityChangeEffect, ConfirmFacilityChangeEvent>()
        .addTransformer(ChangeFacilityEffect::class.java, changeFacility(schedulersProvider.io()))
        .addAction(CloseSheet::class.java, { uiActions.closeSheet() }, schedulersProvider.ui())
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addTransformer(TouchFacilitySyncGroupSwitchedAtTime::class.java, touchFacilitySyncGroupSwitchedAtTime())
        .build()
  }

  private fun changeFacility(
      io: Scheduler
  ): ObservableTransformer<ChangeFacilityEffect, ConfirmFacilityChangeEvent> {
    return ObservableTransformer { changeFacilityStream ->
      changeFacilityStream
          .map { it.selectedFacility }
          .observeOn(io)
          .doOnNext(facilityRepository::setCurrentFacilityImmediate)
          .doOnNext { isFacilitySwitchedPreference.set(true) }
          .doOnNext { clearAndSyncReports(io) }
          .map(::FacilityChanged)
    }
  }

  private fun clearAndSyncReports(scheduler: Scheduler) {
    reportsRepository
        .deleteReports()
        .andThen(reportsSync.sync().onErrorComplete())
        .subscribeOn(scheduler)
        .subscribe()
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, ConfirmFacilityChangeEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { facilityRepository.currentFacilityImmediate() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun touchFacilitySyncGroupSwitchedAtTime(): ObservableTransformer<TouchFacilitySyncGroupSwitchedAtTime, ConfirmFacilityChangeEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { facilitySyncGroupSwitchAtPreference.set(Optional.of(Instant.now(clock))) }
          .map { FacilitySyncGroupSwitchedAtTimeTouched }
    }
  }
}
