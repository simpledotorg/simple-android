package org.simple.clinic.router.screen

import android.content.Context
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashSet

/**
 * Coordinates between multiple child [KeyChangers][KeyChanger].
 */
class NestedKeyChanger : KeyChanger {

  private val linkedChildKeyChangers = LinkedHashSet<KeyChanger>()

  fun add(keyChanger: KeyChanger) {
    linkedChildKeyChangers.add(keyChanger)
  }

  fun remove(keyChanger: KeyChanger) {
    if (!linkedChildKeyChangers.contains(keyChanger)) {
      throw IllegalArgumentException("KeyChanger wasn't registered: $keyChanger")
    }
    linkedChildKeyChangers.remove(keyChanger)
  }

  override fun changeKey(
      outgoingState: State?,
      incomingState: State,
      direction: Direction,
      incomingContexts: Map<Any, Context>,
      callback: TraversalCallback
  ) {
    val keyChangersAlreadyCalled = HashSet<KeyChanger>()
    var anyTraversalComplete = false

    while (true) {
      // Incoming screens can modify the map during iteration.
      val cloneSet = LinkedHashSet(linkedChildKeyChangers)

      val changerList = ArrayList(cloneSet)
      for (i in changerList.indices.reversed()) {
        val keyChanger = changerList[i]
        if (keyChangersAlreadyCalled.contains(keyChanger)) {
          // This will be true only after the first while loop.
          continue
        }
        keyChangersAlreadyCalled.add(keyChanger)

        val subCallback = NoOpTraversalCallbackWithCheck()
        keyChanger.changeKey(outgoingState, incomingState, direction, incomingContexts, subCallback)

        if (subCallback.isComplete) {
          anyTraversalComplete = true
          break
        }
      }

      // Screens can register new key-changers when they are inflated in a recursive manner.
      // In order to let them initialize themselves, we'll have to restart
      if (linkedChildKeyChangers.size <= cloneSet.size) {
        break
      }
    }

    if (anyTraversalComplete) {
      callback.onTraversalCompleted()
    }
  }

  private class NoOpTraversalCallbackWithCheck : TraversalCallback {

    var isComplete: Boolean = false
      private set

    override fun onTraversalCompleted() {
      isComplete = true
    }
  }
}
