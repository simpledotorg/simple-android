package org.simple.mobius.migration.fix

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import org.simple.mobius.migration.fix.CounterEffect.NegativeNumberEffect
import org.simple.mobius.migration.fix.CounterEvent.Decrement
import org.simple.mobius.migration.fix.CounterEvent.Increment

typealias CounterModel = Int

sealed class CounterEvent {
  object Increment : CounterEvent()
  object Decrement : CounterEvent()
}

fun update(
    model: CounterModel,
    event: CounterEvent
): Next<CounterModel, CounterEffect> {
  return when (event) {
    Increment -> next<CounterModel, CounterEffect>(model + 1)
    Decrement -> next<CounterModel, CounterEffect>(model - 1, setOf(NegativeNumberEffect))
  }
}

sealed class CounterEffect {
  object NegativeNumberEffect : CounterEffect()
}
