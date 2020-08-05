package org.simple.clinic.home.help

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class HelpScreenEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: HelpScreenUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: HelpScreenUiActions): HelpScreenEffectHandler
  }

  fun build(): ObservableTransformer<HelpScreenEffect, HelpScreenEvent> = RxMobius
      .subtypeEffectHandler<HelpScreenEffect, HelpScreenEvent>()
      .addAction(ShowLoadingView::class.java, uiActions::showLoadingView, schedulersProvider.ui())
      .build()
}
