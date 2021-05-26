package org.simple.clinic.navigation.v2.compat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.ScreenKey

class ScreenFragmentCompat : Fragment(), HandlesBack {

  companion object {
    fun create() = ScreenFragmentCompat()
  }

  private val key by lazy(LazyThreadSafetyMode.NONE) {
    ScreenKey.key<ScreenKeyCompat>(this).key
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(key.layoutRes(), container, false)
  }

  override fun onBackPressed(): Boolean {
    val screen = requireView()

    return (screen as? HandlesBack)?.onBackPressed() ?: false
  }
}
