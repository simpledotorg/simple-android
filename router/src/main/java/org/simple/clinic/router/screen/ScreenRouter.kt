package org.simple.clinic.router.screen

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import android.view.View
import androidx.annotation.CheckResult
import flow.Direction
import flow.Flow
import flow.History
import flow.KeyChanger
import flow.KeyDispatcher
import flow.KeyParceler
import flow.logSavedStateSizes
import io.reactivex.Observable
import io.reactivex.exceptions.Exceptions
import org.simple.clinic.router.ScreenResultBus
import timber.log.Timber
import java.util.ArrayList

/**
 * Responsible for routing between screens inside the host Activity.
 */
class ScreenRouter(
    private val activity: Activity,
    private val flowSupplier: Supplier<Flow>,
    private val nestedKeyChanger: NestedKeyChanger,
    private val resultBus: ScreenResultBus
) {
  private val backPressInterceptors = ArrayList<BackPressInterceptor>()

  private var flowInstalled: Boolean = false

  private val keyParceler: KeyParceler = DefaultKeyParceler()

  companion object {
    fun create(activity: Activity, nestedKeyChanger: NestedKeyChanger, resultBus: ScreenResultBus): ScreenRouter {
      val flowSupplier = object : Supplier<Flow> {
        override fun get(): Flow {
          return Flow.get(activity)
        }
      }
      return ScreenRouter(activity, flowSupplier, nestedKeyChanger, resultBus)
    }
  }

  fun installInContext(baseContext: Context, initialScreen: FullScreenKey): Context {
    flowInstalled = true

    val keyDispatcher = KeyDispatcher.configure(activity, nestedKeyChanger).build()

    return Flow.configure(baseContext, activity)
        .defaultKey(initialScreen)
        .dispatcher(keyDispatcher)
        .keyParceler(keyParceler)
        .install()
  }

  /**
   * Get the key that was used for inflating a <var>view</var>.
   */
  fun <T> key(view: View): T {
    var name = "<nameless>"
    try {
      name = view.resources.getResourceEntryName(view.id)
    } catch (e: Resources.NotFoundException) {
      // Nothing to see here
    }
    return Flow.getKey<T>(view) ?: throw IllegalStateException("No key found for View: [$name]")
  }

  fun push(screenKey: FullScreenKey) {
    Timber.tag("Screen Router").i("Push: ${screenKey.analyticsName}")
    flow().set(screenKey)
  }

  fun clearHistoryAndPush(screenKey: FullScreenKey, direction: RouterDirection) {
    Timber.tag("Screen Router").i("Clear history and push: ${screenKey.analyticsName}")
    flow().setHistory(History.single(screenKey), direction.flowDirection)
  }

  fun popAndPush(screenKey: FullScreenKey, direction: RouterDirection) {
    Timber.tag("Screen Router").i("Pop and push: ${screenKey.analyticsName}")
    flow().replaceTop(screenKey, direction.flowDirection)
  }

  fun pop(): BackStackPopCallback {
    Timber.tag("Screen Router").i("Pop")
    val popped = flow().goBack()
    return BackStackPopCallback(popped)
  }

  fun replaceKeyOfSameType(screenKey: FullScreenKey) {
    Timber.tag("Screen Router").i("Replace key of type: ${screenKey.analyticsName}")

    val builder = flow().history.buildUpon()

    require(!builder.isEmpty) { "Cannot replace key with empty history!" }

    while (!builder.isEmpty) {
      val top = builder.pop()!!
      if (top.javaClass == screenKey.javaClass) {
        builder.push(screenKey)
        break
      }
    }

    if (builder.isEmpty) {
      throw RuntimeException("Could not find key of type [${screenKey.javaClass.name}] to replace!")
    } else {
      flow().setHistory(builder.build(), Direction.REPLACE)
    }
  }

  @CheckResult
  fun streamScreenResults(): Observable<Any> {
    return resultBus.streamResults()
  }

  fun registerKeyChanger(keyChanger: KeyChanger) {
    nestedKeyChanger.add(keyChanger)
  }

  fun registerBackPressInterceptor(interceptor: BackPressInterceptor) {
    backPressInterceptors.add(interceptor)
  }

  fun unregisterBackPressInterceptor(interceptor: BackPressInterceptor) {
    backPressInterceptors.remove(interceptor)
  }

  fun offerBackPressToInterceptors(): BackPressInterceptCallback {
    val callback = BackPressInterceptCallback()
    for (i in backPressInterceptors.indices.reversed()) {
      backPressInterceptors[i].onInterceptBackPress(callback)
      if (callback.intercepted) {
        return callback
      }
    }
    return callback
  }

  fun logSizesOfSavedStates() {
    try {
      logSavedStateSizes(flow(), keyParceler, activity.resources)
    } catch (e: Throwable) {
      // This is meant to get analytics, don't crash the app if something goes wrong here
      Timber.e(e)
    }
  }

  inline fun <reified T> hasKeyOfType(): Boolean {
    return flow().history.any { it is T }
  }

  fun flow(): Flow {
    try {
      return flowSupplier.get()
    } catch (e: IllegalStateException) {
      if (e.message!!.contains("Context was not wrapped with flow") && flowInstalled) {
        throw UnsupportedOperationException("Routing cannot be done in onCreate(). Wait till onStart()/onPostCreate().")
      } else {
        throw e
      }
    } catch (e: Exception) {
      throw Exceptions.propagate(e)
    }
  }

  private class DefaultKeyParceler : KeyParceler {

    override fun toParcelable(key: Any): Parcelable {
      return key as Parcelable
    }

    override fun toKey(parcelable: Parcelable): Any {
      return parcelable
    }
  }
}
