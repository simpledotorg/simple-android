package org.simple.clinic.signature

import android.graphics.Bitmap
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.io.File

class SignatureEffectHandler @AssistedInject constructor(
    @Assisted private val ui: SignatureUiActions,
    private val schedulersProvider: SchedulersProvider
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(
        ui: SignatureUiActions
    ): SignatureEffectHandler
  }

  fun build(): ObservableTransformer<SignatureEffect,
      SignatureEvent> = RxMobius
      .subtypeEffectHandler<SignatureEffect, SignatureEvent>()
      .addAction(ClearSignature::class.java, ui::signatureCleared, schedulersProvider.ui())
      .addTransformer(AcceptSignature::class.java, acceptSignature())
      .addAction(CloseScreen::class.java, ui::closeScreen, schedulersProvider.ui())
      .build()

  private fun acceptSignature(): ObservableTransformer<AcceptSignature, SignatureEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { acceptSignature ->
            val directory = File(acceptSignature.filePath, "doctor_prescription_signature.png")
            val outputStream = directory.outputStream()
            acceptSignature.bitmap?.compress(Bitmap.CompressFormat.PNG, 70, outputStream)
            outputStream.flush()
            outputStream.close()
          }
          .map { SignatureAccepted }
    }
  }
}
