package org.resolvetosavelives.red.home.bp

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class NewBpScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_new_bp
  }
}
