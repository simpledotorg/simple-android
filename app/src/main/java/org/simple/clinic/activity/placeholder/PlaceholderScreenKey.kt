package org.simple.clinic.activity.placeholder

import android.app.Activity
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.ScreenRouter

/**
 * This is a placeholder screen that does nothing except display a
 * blank view.
 *
 * The only reason this exists is for cases where the
 * screen that must be displayed when creating an [Activity] is because
 *
 * - [ScreenRouter] requires us to pass in a key when constructing it
 * - The actual key that we want to load is conditional and might require some database I/O to figure out
 *
 * // TODO(vs): 2019-10-23 Either remove the requirement from ScreenRouter or remove Flow
 **/
@Parcelize
class PlaceholderScreenKey : FullScreenKey {

  @IgnoredOnParcel
  override val analyticsName: String = "Placeholder"

  override fun layoutRes(): Int = R.layout.screen_placeholder
}
