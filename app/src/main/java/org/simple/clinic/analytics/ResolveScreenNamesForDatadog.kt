package org.simple.clinic.analytics

import androidx.fragment.app.Fragment
import com.datadog.android.rum.tracking.AcceptAllSupportFragments
import org.simple.clinic.navigation.v2.compat.ScreenFragmentCompat
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.navigation.v2.fragments.BaseDialog
import org.simple.clinic.navigation.v2.fragments.BaseScreen

class ResolveScreenNamesForDatadog: AcceptAllSupportFragments() {

  override fun getViewName(component: Fragment): String? {
    return when(component) {
      is ScreenFragmentCompat -> component.screenName
      is BaseScreen<*, *, *, *, *, *> -> component.screenName
      is BaseBottomSheet<*, *, *, *, *, *> -> component.screenName
      is BaseDialog<*, *, *, *, *, *> -> component.screenName
      else -> super.getViewName(component)
    }
  }
}
