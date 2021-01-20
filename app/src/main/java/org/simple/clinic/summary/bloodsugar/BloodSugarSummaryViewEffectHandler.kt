package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarSummaryViewEffectHandler @AssistedInject constructor(
    private val bloodSugarRepository: BloodSugarRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions,
    private val config: BloodSugarSummaryConfig,
    private val currentFacility: Lazy<Facility>
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): BloodSugarSummaryViewEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent>()
        .addTransformer(FetchBloodSugarSummary::class.java, fetchBloodSugarMeasurements(bloodSugarRepository, schedulersProvider.ui()))
        .addTransformer(FetchBloodSugarCount::class.java, fetchBloodSugarMeasurementsCount(schedulersProvider.io()))
        .addTransformer(OpenBloodSugarTypeSelector::class.java, openBloodSugarSelector(schedulersProvider))
        .addConsumer(ShowBloodSugarHistoryScreen::class.java, { uiActions.showBloodSugarHistoryScreen(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(OpenBloodSugarUpdateSheet::class.java, { uiActions.openBloodSugarUpdateSheet(it.measurement.uuid, it.measurement.reading.type) }, schedulersProvider.ui())
        .build()
  }

  private fun openBloodSugarSelector(schedulersProvider: SchedulersProvider): ObservableTransformer<OpenBloodSugarTypeSelector, BloodSugarSummaryViewEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentFacility.get() }
          .observeOn(schedulersProvider.ui())
          .map { uiActions.showBloodSugarTypeSelector(it) }
          .flatMap { Observable.empty<BloodSugarSummaryViewEvent>() }
    }
  }

  private fun fetchBloodSugarMeasurements(
      bloodSugarRepository: BloodSugarRepository,
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodSugarSummary, BloodSugarSummaryViewEvent> {
    return ObservableTransformer { effect ->
      effect
          .flatMap {
            bloodSugarRepository
                .latestMeasurements(it.patientUuid, config.numberOfBloodSugarsToDisplay)
                .subscribeOn(scheduler)
          }
          .map { BloodSugarSummaryFetched(it) }
    }
  }

  private fun fetchBloodSugarMeasurementsCount(
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodSugarCount, BloodSugarSummaryViewEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            bloodSugarRepository
                .bloodSugarsCount(it.patientUuid)
                .subscribeOn(scheduler)
          }
          .map(::BloodSugarCountFetched)
    }
  }
}
