package org.simple.clinic.signature

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider

class SignatureEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val signatureRepository: SignatureRepository,
    @Assisted private val uiActions: SignatureUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: SignatureUiActions): SignatureEffectHandler
  }

  fun build(): ObservableTransformer<SignatureEffect, SignatureEvent> = RxMobius
      .subtypeEffectHandler<SignatureEffect, SignatureEvent>()
      .addAction(ClearSignature::class.java, uiActions::clearSignature, schedulersProvider.ui())
      .addTransformer(AcceptSignature::class.java, acceptSignature())
      .addAction(CloseScreen::class.java, uiActions::closeScreen, schedulersProvider.ui())
      .build()

  private fun acceptSignature(): ObservableTransformer<AcceptSignature, SignatureEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { acceptSignature ->
            signatureRepository.saveSignatureBitmap(acceptSignature.bitmap)
          }
          .map { SignatureAccepted }
    }
  }
}
