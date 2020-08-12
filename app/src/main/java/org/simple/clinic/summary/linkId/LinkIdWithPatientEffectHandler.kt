package org.simple.clinic.summary.linkId

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class LinkIdWithPatientEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: LinkIdWithPatientUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: LinkIdWithPatientUiActions): LinkIdWithPatientEffectHandler
  }

  fun build(): ObservableTransformer<LinkIdWithPatientEffect, LinkIdWithPatientEvent> = RxMobius
      .subtypeEffectHandler<LinkIdWithPatientEffect, LinkIdWithPatientEvent>()
      .addConsumer(RenderIdentifierText::class.java, { uiActions.renderIdentifierText(it.identifier) }, schedulersProvider.ui())
      .build()
}
