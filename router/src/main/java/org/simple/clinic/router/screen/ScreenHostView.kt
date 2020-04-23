package org.simple.clinic.router.screen

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import org.simple.clinic.router.R

class ScreenHostView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), NavHost, LifecycleOwner {

  private val navigationController = NavHostController(context)
  private val lifecycleRegistry = LifecycleRegistry(this)

  private val activity: ComponentActivity
    get() {
      return generateSequence({ context }) { context ->
        when (context) {
          is ComponentActivity -> null
          is ContextWrapper -> context.baseContext
          else -> null
        }
      }.last() as? ComponentActivity ?: throw RuntimeException("Failed to get activity in NavHostView!")
    }

  init {
    val ta = context.resources.obtainAttributes(
        attrs,
        R.styleable.ScreenHostView
    )
    val navGraphId = ta.getResourceId(R.styleable.ScreenHostView_navGraph, 0)
    val screenNavigator = ScreenNavigator(this)

    Navigation.setViewNavController(this, navigationController)

    navigationController.navigatorProvider.addNavigator(screenNavigator)
    navigationController.setGraph(navGraphId)

    navigationController.setLifecycleOwner(this)
    navigationController.setOnBackPressedDispatcher(activity.onBackPressedDispatcher)

    ta.recycle()

    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return

    lifecycleRegistry.currentState = Lifecycle.State.STARTED
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
  }

  override fun onDetachedFromWindow() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    super.onDetachedFromWindow()
  }

  override fun getNavController() = navigationController

  override fun getLifecycle() = lifecycleRegistry
}
