package org.simple.clinic.home.report

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.reports.ReportsRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class ReportsEffectHandler @Inject constructor(
    private val reportsRepository: ReportsRepository,
    private val schedulersProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<ReportsEffect, ReportsEvent> = RxMobius
      .subtypeEffectHandler<ReportsEffect, ReportsEvent>()
      .addTransformer(LoadReports::class.java, loadReports())
      .build()

  private fun loadReports(): ObservableTransformer<LoadReports, ReportsEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { reportsRepository.reportsContentText() }
          .map(::ReportsLoaded)
    }
  }
}
