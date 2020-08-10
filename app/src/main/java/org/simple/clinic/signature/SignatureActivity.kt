package org.simple.clinic.signature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_signature.*
import org.simple.clinic.R

class SignatureActivity : AppCompatActivity(), SignatureUiActions {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_signature)
  }

  override fun clearSignature() {
    drawSignatureFrame.clear()
  }
}

