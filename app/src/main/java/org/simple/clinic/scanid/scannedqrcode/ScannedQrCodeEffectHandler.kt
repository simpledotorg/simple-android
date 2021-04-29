package org.simple.clinic.scanid.scannedqrcode

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ScannedQrCodeEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: ScannedQrCodeUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ScannedQrCodeUiActions): ScannedQrCodeEffectHandler
  }

  fun build(): ObservableTransformer<ScannedQrCodeEffect, ScannedQrCodeEvent> {
    return RxMobius
        .subtypeEffectHandler<ScannedQrCodeEffect, ScannedQrCodeEvent>()
        .addTransformer(SaveNewOngoingPatientEntry::class.java, saveNewPatientEntry())
        .addConsumer(SendBlankScannedQrCodeResult::class.java, { uiActions.sendScannedQrCodeResult(it.scannedQRCodeResult) }, schedulersProvider.ui())
        .build()
  }

  private fun saveNewPatientEntry(): ObservableTransformer<SaveNewOngoingPatientEntry, ScannedQrCodeEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { effect -> patientRepository.saveOngoingEntry(effect.entry) }
          .map { NewOngoingPatientEntrySaved }
    }
  }
}
