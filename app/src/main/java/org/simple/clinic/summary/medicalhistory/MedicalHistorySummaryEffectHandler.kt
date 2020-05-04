package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class MedicalHistorySummaryEffectHandler @Inject constructor(
    private val schedulers: SchedulersProvider,
    private val medicalHistoryRepository: MedicalHistoryRepository
) {

  fun create(): ObservableTransformer<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent>()
        .addTransformer(LoadMedicalHistory::class.java, loadMedicalHistory())
        .build()
  }

  private fun loadMedicalHistory(): ObservableTransformer<LoadMedicalHistory, MedicalHistorySummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { medicalHistoryRepository.historyForPatientOrDefault(it.patientUUID).subscribeOn(schedulers.io()) }
          .map(::MedicalHistoryLoaded)
    }
  }
}
