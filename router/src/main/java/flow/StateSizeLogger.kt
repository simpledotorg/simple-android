package flow

import android.os.Bundle
import android.os.Parcelable
import java.lang.reflect.Modifier

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

/**
 * Repurposed from [InternalLifecycleIntegration.save]
 **/
private fun parcelables(keyManager: KeyManager, parceler: KeyParceler, history: History): List<Bundle> {
  return history
      .reverseIterator<Any>()
      .asSequence()
      .filterNot { it.javaClass.isAnnotationPresent(NotPersistent::class.java) }
      .map { keyManager.getState(it).toBundle(parceler) }
      .toList()
}
