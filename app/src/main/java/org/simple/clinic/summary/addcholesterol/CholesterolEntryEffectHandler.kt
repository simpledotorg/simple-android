package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import javax.inject.Inject

class CholesterolEntryEffectHandler @Inject constructor(
    private val clock: UtcClock,
    private val uuidGenerator: UuidGenerator,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val schedulersProvider: SchedulersProvider,
) {

  fun build(): ObservableTransformer<CholesterolEntryEffect, CholesterolEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CholesterolEntryEffect, CholesterolEntryEvent>()
        .addTransformer(SaveCholesterol:: class.java, saveCholesterol())
        .build()
  }

  private fun saveCholesterol(): ObservableTransformer<SaveCholesterol, CholesterolEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = effect.patientUuid
            )

            medicalHistoryRepository.save(
                medicalHistory.cholesterolChanged(effect.cholesterolValue),
                Instant.now(clock),
            )
          }
          .map { CholesterolSaved }
    }
  }
}
