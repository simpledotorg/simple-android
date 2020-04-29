package org.simple.mobius.migration.fix

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import kotlin.properties.Delegates
import org.simple.mobius.migration.fix.CounterEffect.NegativeNumberEffect
import org.simple.mobius.migration.fix.CounterEvent.Decrement
import org.simple.mobius.migration.fix.CounterEvent.Increment

typealias CounterModel = Int

sealed class CounterEvent {
  object Increment : CounterEvent()
  object Decrement : CounterEvent()
}

class CounterUpdate() : Update<CounterModel, CounterEvent, CounterEffect> {
  override fun update(model: CounterModel, event: CounterEvent): Next<CounterModel, CounterEffect> {
    return when (event) {
      Increment -> next<CounterModel, CounterEffect>(model + 1)
      Decrement -> next<CounterModel, CounterEffect>(model - 1, setOf(NegativeNumberEffect))
    }
  }
}

fun createEffectHandler(view: VerifiableCounterView): ObservableTransformer<CounterEffect, CounterEvent> {
  return RxMobius
      .subtypeEffectHandler<CounterEffect, CounterEvent>()
      .addAction(NegativeNumberEffect::class.java) { view.notifyNegativeNumber() }
      .build()
}

sealed class CounterEffect {
  object NegativeNumberEffect : CounterEffect()
}

class VerifiableCounterView {
  var model by Delegates.notNull<CounterModel>()
  var notifyNegativeNumberInvoked: Boolean = false

  fun render(model: CounterModel) {
    this.model = model
  }

  fun notifyNegativeNumber() {
    this.notifyNegativeNumberInvoked = true
  }
}
