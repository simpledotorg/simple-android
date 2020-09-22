package org.simple.clinic.signature

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.activity_signature.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SignatureActivity : AppCompatActivity(), SignatureUiActions {

  private lateinit var component: SignatureComponent

  @Inject
  lateinit var effectHandlerFactory: SignatureEffectHandler.Factory

  private val events: Observable<SignatureEvent> by unsafeLazy {
    Observable
        .merge(
            acceptSignatureClicks(),
            undoClicks()
        )
        .cast<SignatureEvent>()
  }

  private val mobiusDelegate by unsafeLazy {
    MobiusDelegate.forActivity(
        events = events,
        defaultModel = SignatureModel.create(),
        init = SignatureInit(),
        update = SignatureUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_signature)
  }

  private fun acceptSignatureClicks() = acceptSignature
      .clicks()
      .map {
        val bitmap: Bitmap? = drawSignatureFrame.getTransparentSignatureBitmap(true)
        AcceptClicked(bitmap)
      }

  private fun undoClicks() = clearSignature
      .clicks()
      .map { UndoClicked }

  override fun clearSignature() {
    drawSignatureFrame.clear()
  }

  override fun setSignatureBitmap(signatureBitmap: Bitmap) {
    drawSignatureFrame.signatureBitmap = signatureBitmap
  }

  override fun closeScreen() {
    finish()
  }

  override fun onStart() {
    super.onStart()
    mobiusDelegate.start()
  }

  override fun onStop() {
    super.onStop()
    mobiusDelegate.stop()
  }

  private fun setupDI() {
    component = ClinicApp.appComponent
        .signatureComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun attachBaseContext(newBase: Context?) {
    setupDI()
    super.attachBaseContext(newBase)
  }

}

