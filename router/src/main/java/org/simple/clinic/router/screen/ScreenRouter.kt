package org.simple.clinic.router.screen

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import android.support.annotation.CheckResult
import android.view.View
import flow.Flow
import flow.History
import flow.KeyChanger
import flow.KeyDispatcher
import flow.KeyParceler
import io.reactivex.Observable
import io.reactivex.exceptions.Exceptions
import org.simple.clinic.router.ScreenResultBus
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
        .keyParceler(DefaultKeyParceler())
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
    flow().set(screenKey)
  }

  fun clearHistoryAndPush(screenKey: FullScreenKey, direction: RouterDirection) {
    flow().setHistory(History.single(screenKey), direction.flowDirection)
  }

  fun pop(): BackStackPopCallback {
    val popped = flow().goBack()
    return BackStackPopCallback(popped)
  }

  fun sendResultAndPop(result: Any) {
    // TODO: Now that BaseViewGroupKeyChanger inflates a new view before removing
    // TODO: the older one, this is no longer be required. Remove this after testing.
    // The name is actually incorrect. It's important to send the result only
    // after the previous screen is inflated and has registered for results.
    // But the name is kept in this way to maintain familiarity with how Activity
    // results are sent -- the result is set before the Activity is finished.
    pop()

    resultBus.send(result)
  }

  @CheckResult
  fun streamScreenResults(): Observable<Any> {
    return resultBus.streamResults()
  }

  fun registerKeyChanger(keyChanger: KeyChanger) {
    nestedKeyChanger.add(keyChanger)
  }

  fun unregisterKeyChanger(keyChanger: KeyChanger) {
    nestedKeyChanger.remove(keyChanger)
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

  private fun flow(): Flow {
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
