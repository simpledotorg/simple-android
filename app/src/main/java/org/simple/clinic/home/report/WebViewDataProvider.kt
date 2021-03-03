package org.simple.clinic.home.report

import android.webkit.JavascriptInterface
import dagger.Lazy
import org.simple.clinic.facility.Facility
import javax.inject.Inject

class WebViewDataProvider @Inject constructor(
    private val currentFacility: Lazy<Facility>
) {

  @JavascriptInterface
  fun currentFacility(): String {
    return currentFacility.get().uuid.toString()
  }
}
