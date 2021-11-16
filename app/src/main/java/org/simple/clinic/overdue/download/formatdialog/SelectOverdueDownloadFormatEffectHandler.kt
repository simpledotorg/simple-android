package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.download.OverdueListDownloader
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class SelectOverdueDownloadFormatEffectHandler @Inject constructor(
    private val overdueListDownloader: OverdueListDownloader,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<SelectOverdueDownloadFormatEffect, SelectOverdueDownloadFormatEvent> = RxMobius
      .subtypeEffectHandler<SelectOverdueDownloadFormatEffect, SelectOverdueDownloadFormatEvent>()
      .addTransformer(DownloadForShare::class.java, downloadForShare())
      .build()

  private fun downloadForShare(): ObservableTransformer<DownloadForShare, SelectOverdueDownloadFormatEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { overdueListDownloader.download(it.downloadFormat) }
          .map(::FileDownloadedForSharing)
    }
  }
}
