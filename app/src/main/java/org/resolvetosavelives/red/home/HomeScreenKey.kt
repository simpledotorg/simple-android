package org.resolvetosavelives.red.home

import android.annotation.SuppressLint
import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
@SuppressLint("ParcelCreator")
class HomeScreenKey : FullScreenKey {

  override fun layoutRes(): Int {
    return R.layout.screen_home
  }
}
