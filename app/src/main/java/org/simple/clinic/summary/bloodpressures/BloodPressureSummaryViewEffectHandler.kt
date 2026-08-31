package org.simple.clinic.summary.bloodpressures

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class BloodPressureSummaryViewEffectHandler @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val schedulersProvider: SchedulersProvider,
    private val facility: Lazy<Facility>,
    @Assisted private val uiActions: BloodPressureSummaryViewUiActions,
    private val uuidGenerator: UuidGenerator,
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: BloodPressureSummaryViewUiActions): BloodPressureSummaryViewEffectHandler
  }

  fun build(): ObservableTransformer<BloodPressureSummaryViewEffect, BloodPressureSummaryViewEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureSummaryViewEffect, BloodPressureSummaryViewEvent>()
        .addTransformer(LoadBloodPressures::class.java, loadBloodPressureHistory(schedulersProvider.io()))
        .addTransformer(LoadBloodPressuresCount::class.java, loadBloodPressuresCount(schedulersProvider.io()))
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addConsumer(OpenBloodPressureEntrySheet::class.java, { uiActions.openBloodPressureEntrySheet(it.patientUuid, it.currentFacility) }, schedulersProvider.ui())
        .addConsumer(OpenBloodPressureUpdateSheet::class.java, { uiActions.openBloodPressureUpdateSheet(it.id) }, schedulersProvider.ui())
        .addConsumer(ShowBloodPressureHistoryScreen::class.java, { uiActions.showBloodPressureHistoryScreen(it.patientUuid) }, schedulersProvider.ui())
        .addTransformer(LoadMedicalHistory::class.java, loadMedicalHistory(schedulersProvider.io()))
        .build()
  }

  private fun loadBloodPressureHistory(
      scheduler: Scheduler
  ): ObservableTransformer<LoadBloodPressures, BloodPressureSummaryViewEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap {
            bloodPressureRepository
                .newestMeasurementsForPatient(it.patientUuid, it.numberOfBpsToDisplay)
                .subscribeOn(scheduler)
          }
          .map(::BloodPressuresLoaded)
    }
  }

  private fun loadBloodPressuresCount(
      scheduler: Scheduler
  ): ObservableTransformer<LoadBloodPressuresCount, BloodPressureSummaryViewEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap {
            bloodPressureRepository
                .bloodPressureCount(it.patientUuid)
                .subscribeOn(scheduler)
          }
          .map(::BloodPressuresCountLoaded)
    }
  }

  private fun loadCurrentFacility(scheduler: Scheduler): ObservableTransformer<LoadCurrentFacility, BloodPressureSummaryViewEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { facility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadMedicalHistory(
      scheduler: Scheduler
  ): ObservableTransformer<LoadMedicalHistory, BloodPressureSummaryViewEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .flatMap {
            medicalHistoryRepository.historyForPatientOrDefault(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = it.patientUuid
            )
          }
          .map(::MedicalHistoryLoaded)
    }
  }
}
