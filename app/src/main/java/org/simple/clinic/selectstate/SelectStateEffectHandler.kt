package org.simple.clinic.selectstate

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class SelectStateEffectHandler @Inject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val schedulers: SchedulersProvider
) {

  fun build(): ObservableTransformer<SelectStateEffect, SelectStateEvent> = RxMobius
      .subtypeEffectHandler<SelectStateEffect, SelectStateEvent>()
      .addTransformer(LoadStates::class.java, loadStates())
      .addTransformer(SaveSelectedState::class.java, saveState())
      .build()

  private fun saveState(): ObservableTransformer<SaveSelectedState, SelectStateEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { appConfigRepository.saveState(it.state) }
          .map { StateSaved }
    }
  }

  private fun loadStates(): ObservableTransformer<LoadStates, SelectStateEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { appConfigRepository.fetchStatesInSelectedCountry() }
          .map(::StatesResultFetched)
    }
  }
}
