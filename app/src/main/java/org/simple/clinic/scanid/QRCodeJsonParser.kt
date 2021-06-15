package org.simple.clinic.scanid

import com.squareup.moshi.Moshi
import javax.inject.Inject

class QRCodeJsonParser @Inject constructor(
    moshi: Moshi
) {
  private val adapter = moshi.adapter(IndiaNHIDInfoPayload::class.java)

  fun parseQRCodeJson(text: String): IndiaNHIDInfoPayload? {
    return adapter.fromJson(text)
  }
}
