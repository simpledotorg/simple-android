package org.simple.clinic.navigation

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.NavigationRes
import androidx.customview.view.AbsSavedState
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

  @NavigationRes
  private var graphId: Int = 0

  init {
    val ta = context.resources.obtainAttributes(
        attrs,
        R.styleable.ScreenHostView
    )
    graphId = ta.getResourceId(R.styleable.ScreenHostView_screenNavGraph, 0)
    val screenNavigator = ScreenNavigator(this)

    Navigation.setViewNavController(this, navigationController)

    navigationController.navigatorProvider.addNavigator(screenNavigator)
    // This is to ensure the app doesn't crash when we are setting the graph dynamically.
    // We will be setting this onInflate
    if (graphId != 0) {
      navigationController.setGraph(graphId)
    }

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

  override fun onSaveInstanceState(): Parcelable? {
    val superState = super.onSaveInstanceState()
    val savedState = SavedState(superState)
    savedState.navControllerState = navigationController.saveState()
    savedState.graphId = graphId
    return savedState
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state !is SavedState) {
      super.onRestoreInstanceState(state)
      return
    }
    super.onRestoreInstanceState(state.superState)
    navigationController.restoreState(state.navControllerState)
    if (graphId != 0) {
      navigationController.setGraph(state.graphId)
    }
  }

  class SavedState : AbsSavedState {
    var navControllerState: Bundle? = null
    var graphId: Int = 0

    constructor(superState: Parcelable?) : super(superState!!)
    constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
      navControllerState = source.readBundle(loader)
      graphId = source.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
      super.writeToParcel(out, flags)
      out.writeBundle(navControllerState)
      out.writeInt(graphId)
    }

    companion object {
      @JvmField
      val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.ClassLoaderCreator<SavedState> {
        override fun createFromParcel(
            `in`: Parcel,
            loader: ClassLoader
        ): SavedState {
          return SavedState(`in`, loader)
        }

        override fun createFromParcel(`in`: Parcel): SavedState {
          return SavedState(`in`, null)
        }

        override fun newArray(size: Int): Array<SavedState?> {
          return arrayOfNulls(size)
        }
      }
    }
  }
}
