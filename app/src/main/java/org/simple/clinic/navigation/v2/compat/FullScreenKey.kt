package org.simple.clinic.navigation.v2.compat

import android.os.Parcelable
import androidx.annotation.LayoutRes

/**
 *
 * Screens can receive payloads inside their associated keys by calling [org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider.keyFor].
 *
 * Note: use AutoValue or otherwise ensure equals() is overridden.
 * Screen routing is skipped if an outgoing key and an incoming key are equal.
 */
interface FullScreenKey : Parcelable {

  val analyticsName: String

  @LayoutRes
  fun layoutRes(): Int
}
