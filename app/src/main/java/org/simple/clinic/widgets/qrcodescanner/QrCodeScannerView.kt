package org.simple.clinic.widgets.qrcodescanner

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import org.simple.clinic.R

class QrCodeScannerView @JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

  init {
    View.inflate(context, R.layout.view_qrcode_scanner, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
  }
}
