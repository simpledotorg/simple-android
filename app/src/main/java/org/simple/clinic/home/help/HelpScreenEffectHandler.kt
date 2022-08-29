package org.simple.clinic.home.help

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.help.HelpRepository
import org.simple.clinic.help.HelpSync
import org.simple.clinic.util.scheduler.SchedulersProvider

class HelpScreenEffectHandler @AssistedInject constructor(
    private val helpRepository: HelpRepository,
    private val helpSync: HelpSync,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<HelpScreenViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<HelpScreenViewEffect>
    ): HelpScreenEffectHandler
  }

  fun build(): ObservableTransformer<HelpScreenEffect, HelpScreenEvent> = RxMobius
      .subtypeEffectHandler<HelpScreenEffect, HelpScreenEvent>()
      .addTransformer(LoadHelpContent::class.java, loadHelpContent())
      .addTransformer(SyncHelp::class.java, syncHelp())
      .addConsumer(HelpScreenViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun syncHelp(): ObservableTransformer<SyncHelp, HelpScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMapSingle { helpSync.pullWithResult() }
          .map(::HelpSyncPullResult)
    }
  }

  private fun loadHelpContent(): ObservableTransformer<LoadHelpContent, HelpScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap { helpRepository.helpContentText() }
          .map(::HelpContentLoaded)
    }
  }
}
