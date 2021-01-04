package org.simple.clinic.navigation.v2

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment

abstract class ScreenKey: Parcelable {

  companion object {
    private const val ARGS_KEY = "org.simple.clinic.navigation.v2.ScreenKey.ARGS_KEY"

    fun <T: ScreenKey> key(fragment: Fragment): T {
      return fragment.requireArguments().getParcelable(ARGS_KEY)!!
    }
  }

  abstract val analyticsName: String

  open val fragmentTag: String
    get() = javaClass.name

  open val type: ScreenType = ScreenType.FullScreen

  val isModal: Boolean
    get() = type == ScreenType.Modal

  fun createFragment(): Fragment {
    return instantiateFragment().apply {
      val args = Bundle()
      args.putParcelable(ARGS_KEY, this@ScreenKey)
      this.arguments = args
    }
  }

  open fun matchesScreen(other: ScreenKey): Boolean {
    return javaClass == other.javaClass
  }

  protected abstract fun instantiateFragment(): Fragment

  enum class ScreenType {
    FullScreen,
    Modal
  }
}
