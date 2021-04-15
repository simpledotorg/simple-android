package org.simple.clinic.navigation.v2.compat

import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class ScreenKeyCompat(val key: FullScreenKey) : ScreenKey() {

  override val fragmentTag: String
    get() = key.javaClass.name

  override val analyticsName: String
    get() = key.analyticsName

  override fun instantiateFragment() = ScreenFragmentCompat.create()

  override fun matchesScreen(other: ScreenKey): Boolean {
    return if (other is ScreenKeyCompat)
      key.javaClass == other.key.javaClass
    else
      false
  }
}

fun FullScreenKey.wrap(): ScreenKey {
  return ScreenKeyCompat(this)
}
