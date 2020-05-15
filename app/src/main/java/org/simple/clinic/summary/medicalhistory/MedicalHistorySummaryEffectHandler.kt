package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Instant

class MedicalHistorySummaryEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val clock: UtcClock,
    private val currentFacility: Lazy<Facility>,
    @Assisted private val uiActions: MedicalHistorySummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: MedicalHistorySummaryUiActions): MedicalHistorySummaryEffectHandler
  }

  fun build(): ObservableTransformer<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent>()
        .addTransformer(LoadMedicalHistory::class.java, loadMedicalHistory())
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addAction(HideDiagnosisError::class.java, uiActions::hideDiagnosisError, schedulers.ui())
        .addConsumer(SaveUpdatedMedicalHistory::class.java, { updateMedicalHistory(it.medicalHistory) }, schedulers.io())
        .build()
  }

  private fun loadMedicalHistory(): ObservableTransformer<LoadMedicalHistory, MedicalHistorySummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { medicalHistoryRepository.historyForPatientOrDefault(it.patientUUID).subscribeOn(schedulers.io()) }
          .map(::MedicalHistoryLoaded)
    }
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, MedicalHistorySummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun updateMedicalHistory(medicalHistory: MedicalHistory) {
    medicalHistoryRepository.save(medicalHistory, Instant.now(clock))
  }
}
