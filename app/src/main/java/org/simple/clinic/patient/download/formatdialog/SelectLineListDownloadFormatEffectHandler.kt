package org.simple.clinic.patient.download.formatdialog

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.download.PatientLineListScheduler
import javax.inject.Inject

class SelectLineListDownloadFormatEffectHandler @Inject constructor(
    private val patientLineListScheduler: PatientLineListScheduler
) {

  fun build(): ObservableTransformer<PatientLineListDownloadFormatEffect, SelectPatientLineListDownloadFormatEvent> = RxMobius
      .subtypeEffectHandler<PatientLineListDownloadFormatEffect, SelectPatientLineListDownloadFormatEvent>()
      .addConsumer(SchedulePatientLineListDownload::class.java, ::scheduleDownload)
      .build()

  private fun scheduleDownload(effect: SchedulePatientLineListDownload) {
    patientLineListScheduler.schedule(effect.fileFormat)
  }
}
