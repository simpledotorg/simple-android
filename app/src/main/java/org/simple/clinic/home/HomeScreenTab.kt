package org.simple.clinic.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.simple.clinic.router.screen.FullScreenKey

class HomeScreenTab : Fragment() {

  companion object {
    private const val ARG_SCREEN_KEY = "arg_screen_key"

    fun create(key: FullScreenKey): HomeScreenTab {
      return HomeScreenTab().apply {
        val args = Bundle()
        args.putParcelable(ARG_SCREEN_KEY, key)

        arguments = args
      }
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    val screenKey = requireArguments()[ARG_SCREEN_KEY] as FullScreenKey

    return inflater.inflate(screenKey.layoutRes(), container, false)
  }
}
