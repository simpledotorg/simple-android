package org.simple.clinic.selectstate

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.StatesResult
import org.simple.clinic.util.scheduler.SchedulersProvider

class SelectStateEffectHandler @AssistedInject constructor(
    private val appConfigRepository: AppConfigRepository,
    private val schedulers: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<SelectStateViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<SelectStateViewEffect>): SelectStateEffectHandler
  }

  fun build(): ObservableTransformer<SelectStateEffect, SelectStateEvent> = RxMobius
      .subtypeEffectHandler<SelectStateEffect, SelectStateEvent>()
      .addTransformer(LoadStates::class.java, loadStates())
      .addTransformer(SaveSelectedState::class.java, saveState())
      .addConsumer(SelectStateViewEffect::class.java, viewEffectsConsumer::accept)
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
          .map(::statesResultToEvent)
    }
  }

  private fun statesResultToEvent(statesResult: StatesResult): SelectStateEvent {
    return when (statesResult) {
      is StatesResult.StatesFetched -> StatesFetched(statesResult.states)
      is StatesResult.FetchError -> FailedToFetchStates(StatesFetchError.fromResolvedError(statesResult.error))
    }
  }
}
