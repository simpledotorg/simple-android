package org.simple.mobius.migration.fix

import com.spotify.mobius.First
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.mobius.migration.fix.EveEffect.BEffect
import org.simple.mobius.migration.fix.EveEffect.CEffect
import org.simple.mobius.migration.fix.EveEvent.BEvent
import org.simple.mobius.migration.fix.EveEvent.CEvent

typealias EveModel = Char

const val defaultModel: EveModel = 'a'

sealed class EveEvent {
  object BEvent : EveEvent()
  object CEvent : EveEvent()
}

sealed class EveEffect {
  object BEffect : EveEffect()
  object CEffect : EveEffect()
}

fun eveInit(model: EveModel): First<EveModel, EveEffect> {
  return First.first(model, setOf(BEffect))
}

fun eveUpdate(
    @Suppress("UNUSED_PARAMETER") model: EveModel,
    event: EveEvent
): Next<EveModel, EveEffect> {
  return when (event) {
    BEvent -> next('b', setOf(CEffect))
    CEvent -> next('c')
  }
}

fun eveEffectHandler(): ObservableTransformer<EveEffect, EveEvent> {
  return RxMobius
      .subtypeEffectHandler<EveEffect, EveEvent>()
      .addTransformer(BEffect::class.java) { bEffects -> bEffects.map { BEvent } }
      .addTransformer(CEffect::class.java) { cEffects -> cEffects.map { CEvent } }
      .build()
}
