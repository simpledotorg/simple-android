package org.simple.clinic.mobius

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch

fun <M, F> justEffect(effect: F): Next<M, F> =
    justEffects(effect)

fun <M, F> next(model: M, vararg effects: F): Next<M, F> = if (effects.isEmpty()) {
  Next.next(model)
} else {
  Next.next(model, setOf(*effects))
}

fun <M, F> justEffects(effect: F, vararg effects: F): Next<M, F> =
    dispatch<M, F>(setOf(effect, *effects))
