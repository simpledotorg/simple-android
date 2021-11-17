package org.simple.clinic.overdue.download.formatdialog

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.download.OverdueDownloadScheduler
import org.simple.clinic.overdue.download.OverdueListDownloader
import org.simple.clinic.util.scheduler.SchedulersProvider

class SelectOverdueDownloadFormatEffectHandler @AssistedInject constructor(
    private val overdueListDownloader: OverdueListDownloader,
    private val schedulersProvider: SchedulersProvider,
    private val overdueDownloadScheduler: OverdueDownloadScheduler,
    @Assisted private val viewEffectsConsumer: Consumer<SelectOverdueDownloadFormatViewEffect>
) {

  @AssistedFactory
  interface Factory {

    fun create(
        viewEffectsConsumer: Consumer<SelectOverdueDownloadFormatViewEffect>
    ): SelectOverdueDownloadFormatEffectHandler
  }

  fun build(): ObservableTransformer<SelectOverdueDownloadFormatEffect, SelectOverdueDownloadFormatEvent> = RxMobius
      .subtypeEffectHandler<SelectOverdueDownloadFormatEffect, SelectOverdueDownloadFormatEvent>()
      .addTransformer(DownloadForShare::class.java, downloadForShare())
      .addConsumer(SelectOverdueDownloadFormatViewEffect::class.java, viewEffectsConsumer::accept)
      .addTransformer(ScheduleDownload::class.java, scheduleDownload())
      .build()

  private fun scheduleDownload(): ObservableTransformer<ScheduleDownload, SelectOverdueDownloadFormatEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { overdueDownloadScheduler.schedule(it.downloadFormat) }
          .map { OverdueDownloadScheduled }
    }
  }

  private fun downloadForShare(): ObservableTransformer<DownloadForShare, SelectOverdueDownloadFormatEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { overdueListDownloader.download(it.downloadFormat) }
          .map(::FileDownloadedForSharing)
    }
  }
}
