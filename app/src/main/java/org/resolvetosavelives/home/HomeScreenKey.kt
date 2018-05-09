package org.resolvetosavelives.home

import android.annotation.SuppressLint
import android.support.annotation.LayoutRes
import kotlinx.android.parcel.Parcelize
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.router.screen.FullScreenKey

@Parcelize
@SuppressLint("ParcelCreator")
data class HomeScreenKey(@LayoutRes private val layoutRes: Int = R.layout.screen_home) : FullScreenKey {

  override fun layoutRes(): Int {
    return layoutRes
  }
}
