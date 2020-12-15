package org.simple.clinic.signature

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ClinicApp
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ActivitySignatureBinding
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SignatureActivity : AppCompatActivity(), SignatureUiActions {

  companion object {

    fun intent(context: Context): Intent {
      return Intent(context, SignatureActivity::class.java)
    }
  }

  private lateinit var component: SignatureComponent

  @Inject
  lateinit var effectHandlerFactory: SignatureEffectHandler.Factory

  private val events by unsafeLazy {
    Observable
        .merge(
            acceptSignatureClicks(),
            undoClicks()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val mobiusDelegate by unsafeLazy {
    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = SignatureModel.create(),
        init = SignatureInit(),
        update = SignatureUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  private lateinit var binding: ActivitySignatureBinding

  private val signatureRoot
    get() = binding.signatureRoot

  private val acceptSignature
    get() = binding.acceptSignature

  private val drawSignatureFrame
    get() = binding.drawSignatureFrame

  private val clearSignature
    get() = binding.clearSignature

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivitySignatureBinding.inflate(layoutInflater)
    setContentView(binding.root)

    signatureRoot.setOnClickListener { finish() }
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

