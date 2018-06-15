package org.simple.clinic.home.bp

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
class NewBpScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_new_bp
  }
}
