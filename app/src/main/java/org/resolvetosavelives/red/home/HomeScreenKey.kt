package org.resolvetosavelives.red.home

import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
class HomeScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_home
  }
}
