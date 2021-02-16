package org.simple.clinic.navigation.v2.keyprovider

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.ScreenFragmentCompat
import org.simple.clinic.navigation.v2.compat.ScreenKeyCompat
import javax.inject.Inject

class FragmentBasedScreenKeyProvider @Inject constructor() : ScreenKeyProvider {

  @Suppress("UNCHECKED_CAST")
  override fun <T> keyFor(view: View): T {
    val fragment = FragmentManager.findFragment<Fragment>(view)

    return if (fragment is ScreenFragmentCompat) {
      ScreenKey.key<ScreenKeyCompat>(fragment).key as T
    } else {
      ScreenKey.key<ScreenKey>(fragment) as T
    }
  }
}
