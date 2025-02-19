package org.simple.clinic.summary.addcholesterol

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant

class CholesterolEntryEffectHandler @AssistedInject constructor(
    private val clock: UtcClock,
    private val uuidGenerator: UuidGenerator,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectConsumer: Consumer<CholesterolEntryViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectConsumer: Consumer<CholesterolEntryViewEffect>): CholesterolEntryEffectHandler
  }

  fun build(): ObservableTransformer<CholesterolEntryEffect, CholesterolEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CholesterolEntryEffect, CholesterolEntryEvent>()
        .addTransformer(SaveCholesterol::class.java, saveCholesterol())
        .addConsumer(CholesterolEntryViewEffect::class.java, viewEffectConsumer::accept)
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
