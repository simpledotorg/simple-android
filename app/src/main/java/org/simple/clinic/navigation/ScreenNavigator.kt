package org.simple.clinic.navigation

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.getStringOrThrow
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.router.R
import java.util.ArrayDeque

@Navigator.Name("screen")
class ScreenNavigator(
    private val container: ViewGroup
) : Navigator<ScreenNavigator.Destination>() {

  companion object {
    private const val KEY_BACK_STACK = "org.simple.clinic.router.screen:ScreenNavigator:backStack"
    private const val KEY_OUTGOING = "org.simple.clinic.router.screen:ScreenNavigator:outgoing"
  }

  private val backStack = ArrayDeque<BackStackEntry>()

  private var outgoingScreen: BackStackEntry? = null

  override fun navigate(destination: Destination, args: Bundle?, navOptions: NavOptions?, navigatorExtras: Extras?): NavDestination? {
    val destinationLayoutRes = destination.layoutRes

    val destinationView = instantiateView(destinationLayoutRes)
    replaceView(destinationView)

    val incomingScreen = BackStackEntry(
        destinationLayoutRes,
        destination.analyticsName
    )

    Analytics.reportScreenChange(outgoingScreen?.analyticsName ?: "", incomingScreen.analyticsName)
    backStack.push(incomingScreen)
    return destination
  }

  override fun createDestination(): Destination = Destination(this)

  override fun popBackStack(): Boolean {
    if (backStack.isEmpty()) return false

    // Removing the last item from the back stack and assiging
    // it to
    outgoingScreen = backStack.pop()

    // Once last item is removed, we are getting the new last item
    // and using it as destination
    val backStackItem = backStack.peekLast()
    if (backStackItem != null) {
      val destinationView = instantiateView(backStackItem.layoutRes)
      replaceView(destinationView)
    }
    return true
  }

  private fun instantiateView(destinationLayoutRes: Int): View {
    return LayoutInflater.from(container.context).inflate(destinationLayoutRes, container, false)
  }

  private fun replaceView(destinationView: View) {
    container.apply {
      removeAllViews()
      addView(destinationView)
    }
  }

  override fun onSaveState(): Bundle? {
    val bundle = Bundle()
    val backStack = this.backStack.toTypedArray()
    bundle.putParcelableArray(KEY_BACK_STACK, backStack)
    bundle.putParcelable(KEY_OUTGOING, outgoingScreen)
    return bundle
  }

  override fun onRestoreState(savedState: Bundle) {
    val savedBackStack = savedState.getParcelableArray(KEY_BACK_STACK)
    backStack.addAll(
        savedBackStack?.map { entry ->
          entry as BackStackEntry
        }.orEmpty()
    )
    outgoingScreen = savedState.getParcelable(KEY_OUTGOING)
  }

  @NavDestination.ClassType(View::class)
  class Destination : NavDestination {

    @LayoutRes
    var layoutRes: Int = 0
      private set

    private var _analyticsName: String? = null
    val analyticsName: String
      get() = _analyticsName!!

    constructor(navigatorProvider: NavigatorProvider) : this(
        navigatorProvider.getNavigator(
            ScreenNavigator::class.java
        )
    )

    constructor(navigator: Navigator<out Destination?>) : super(navigator)

    override fun onInflate(context: Context, attrs: AttributeSet) {
      super.onInflate(context, attrs)
      context.resources.obtainAttributes(attrs, R.styleable.ScreenNavigator).apply {
        layoutRes = getResourceIdOrThrow(R.styleable.ScreenNavigator_layout)
        _analyticsName = getStringOrThrow(R.styleable.ScreenNavigator_analyticsName)
        recycle()
      }
    }
  }
}
