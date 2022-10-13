package org.simple.clinic.patient.download.formatdialog

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.download.PatientLineListScheduler

class SelectLineListDownloadFormatEffectHandler @AssistedInject constructor(
    private val patientLineListScheduler: PatientLineListScheduler,
    @Assisted private val viewEffectsConsumer: Consumer<SelectLineListFormatViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<SelectLineListFormatViewEffect>
    ): SelectLineListDownloadFormatEffectHandler
  }

  fun build(): ObservableTransformer<PatientLineListDownloadFormatEffect, SelectPatientLineListDownloadFormatEvent> = RxMobius
      .subtypeEffectHandler<PatientLineListDownloadFormatEffect, SelectPatientLineListDownloadFormatEvent>()
      .addConsumer(SchedulePatientLineListDownload::class.java, ::scheduleDownload)
      .addConsumer(SelectLineListFormatViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun scheduleDownload(effect: SchedulePatientLineListDownload) {
    patientLineListScheduler.schedule(effect.fileFormat)
  }
}
