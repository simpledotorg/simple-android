package flow

import android.content.res.Resources
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import org.simple.clinic.router.screen.FullScreenKey
import timber.log.Timber
import java.lang.reflect.Modifier

fun logSavedStateSizes(flow: Flow, keyParceler: KeyParceler, resources: Resources) {
  val bundlesToBeSaved = parcelables(getKeyManagerUnsafe(flow), keyParceler, flow.filteredHistory)

  bundlesToBeSaved.forEach { logBundleSize(it, resources) }
}

/**
 * Repurposed from [InternalLifecycleIntegration.save]
 **/
private fun parcelables(
    keyManager: KeyManager,
    parceler: KeyParceler,
    history: History
): List<Bundle> {
  return history
      .reverseIterator<Any>()
      .asSequence()
      .filterNot { it.javaClass.isAnnotationPresent(NotPersistent::class.java) }
      .map { keyManager.getState(it).toBundle(parceler) }
      .toList()
}

/**
 * This method retrieves the current [KeyManager] from [Flow].
 *
 * We have added this method temporarily to track down the source of a crash when the state is
 * getting saved because the [KeyManager] is the class that is responsible for tracking all the
 * states of the screens and there is no other way to get access.
 *
 * The reason this is **Unsafe** is because the keyManager is a private property within the
 * [Flow] instance.
 **/
private fun getKeyManagerUnsafe(flow: Flow): KeyManager {
  val keyManagerField = flow.javaClass.getDeclaredField("keyManager")

  if (Modifier.isPrivate(keyManagerField.modifiers)) {
    keyManagerField.isAccessible = true
  }

  return keyManagerField.get(flow) as KeyManager
}

private fun logBundleSize(bundle: Bundle, resources: Resources) {
  val screenKey = bundle["KEY"] as FullScreenKey

  val viewIds = bundle.getIntArray("VIEW_STATE_IDS")
  val viewStateSizeMap = viewIds!!
      .map { viewId -> bundle.getSparseParcelableArray<Parcelable>("VIEW_STATE_$viewId")!! }
      .map { viewAndChildStates -> viewAndChildStates.sizesOfViewStates(resources) }

  Timber.tag("SaveState").i("Key: ${screenKey.javaClass.name}; Size: ${screenKey.parceledSize()}")
  Timber.tag("SaveState").i("Key Value: $screenKey")
  Timber.tag("SaveState").i("View States: $viewStateSizeMap")
}

private fun SparseArray<Parcelable>.sizesOfViewStates(resources: Resources): Map<String, Int> {
  return (0 until size())
      .map {
        val viewId = keyAt(it)
        val viewName = try {
          resources.getResourceEntryName(viewId)
        } catch (e: Resources.NotFoundException) {
          viewId.toString()
        }
        val viewState = valueAt(it)

        viewName to viewState.parceledSize()
      }
      .toMap()
}

private fun Parcelable.parceledSize(): Int {
  return with(Parcel.obtain()) {
    writeToParcel(this, 0)
    marshall().size
  }
}
