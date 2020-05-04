package org.simple.clinic.navigation

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import org.simple.clinic.R

class ScreenHostView : Fragment(), NavHost {

  companion object {
    private const val KEY_NAV_STATE = "org.simple.clinic.navigation:ScreenHostView:navState"
    private const val KEY_GRAPH_ID = "org.simple.clinic.navigation:ScreenHostView:graphId"
  }

  @NavigationRes
  private var graphId: Int = 0

  private var navigationController: NavHostController? = null

  override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
    super.onInflate(context, attrs, savedInstanceState)
    val typedArray = context.resources.obtainAttributes(
        attrs,
        androidx.navigation.R.styleable.NavHost
    )
    graphId = typedArray.getResourceId(androidx.navigation.R.styleable.NavHost_navGraph, 0)
    typedArray.recycle()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val screenHostView = inflater.inflate(R.layout.fragment_screen_host, container, false)

    navigationController = NavHostController(requireContext())
    navigationController?.setLifecycleOwner(viewLifecycleOwner)
    navigationController?.setOnBackPressedDispatcher(requireActivity().onBackPressedDispatcher)
    onCreateNavController(navController, screenHostView.findViewById(R.id.contentRoot))

    var navState: Bundle? = null
    if (savedInstanceState != null) {
      navState = savedInstanceState.getBundle(KEY_NAV_STATE)
      graphId = savedInstanceState.getInt(KEY_GRAPH_ID)
    }

    if (navState != null) {
      navigationController?.restoreState(navState)
    }
    if (graphId != 0) {
      navigationController?.setGraph(graphId)
    }

    return screenHostView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Navigation.setViewNavController(view, navigationController)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val navState = navigationController?.saveState()
    outState.putBundle(KEY_NAV_STATE, navState)
    outState.putInt(KEY_GRAPH_ID, graphId)
  }

  private fun onCreateNavController(navController: NavController, container: ViewGroup) {
    navController.navigatorProvider.addNavigator(ScreenNavigator(container))
  }

  override fun getNavController(): NavController {
    checkNotNull(navigationController) { "NavController is not available before onCreate()" }
    return navigationController!!
  }
}
