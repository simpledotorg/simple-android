package org.simple.clinic.scanid

import com.squareup.moshi.Moshi
import javax.inject.Inject

class QRCodeJsonParser @Inject constructor(
    private val moshi: Moshi
) {

  fun parseQRCodeJson(text: String): IndiaNHIDInfoPayload? {
    val adapter = moshi.adapter(IndiaNHIDInfoPayload::class.java)
    return adapter.fromJson(text)
  }
}
