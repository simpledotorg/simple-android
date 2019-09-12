package org.simple.mobius.migration.fix

import com.spotify.mobius.First
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import org.simple.mobius.migration.fix.CounterEffect.NegativeNumberEffect
import org.simple.mobius.migration.fix.CounterEffect.NumberZeroEffect
import org.simple.mobius.migration.fix.CounterEvent.Decrement
import org.simple.mobius.migration.fix.CounterEvent.Increment

typealias CounterModel = Int

sealed class CounterEvent {
  object Increment : CounterEvent()
  object Decrement : CounterEvent()
}

fun init(model: CounterModel): First<CounterModel, CounterEffect> {
  return First.first(model, setOf(NumberZeroEffect))
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
  object NumberZeroEffect : CounterEffect()
  object NegativeNumberEffect : CounterEffect()
}
