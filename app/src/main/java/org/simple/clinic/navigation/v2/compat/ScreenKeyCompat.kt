package org.simple.clinic.navigation.v2.compat

import kotlinx.android.parcel.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class ScreenKeyCompat(val key: FullScreenKey) : ScreenKey() {

  override val fragmentTag: String
    get() = key.javaClass.name

  override fun instantiateFragment() = ScreenFragmentCompat.create()
}

fun FullScreenKey.wrap(): ScreenKey {
  return ScreenKeyCompat(this)
}
