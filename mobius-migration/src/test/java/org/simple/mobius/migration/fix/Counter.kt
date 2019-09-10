package org.simple.mobius.migration.fix

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import org.simple.mobius.migration.fix.CounterEvent.Increment

typealias CounterModel = Int

sealed class CounterEvent {
  object Increment : CounterEvent()
}

fun update(
    model: CounterModel,
    event: CounterEvent
): Next<CounterModel, Nothing> {
  return when (event) {
    is Increment -> next<CounterModel, Nothing>(model + 1)
  }
}
