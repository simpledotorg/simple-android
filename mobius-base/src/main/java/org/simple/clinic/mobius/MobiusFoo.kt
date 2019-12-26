package org.simple.clinic.mobius

import com.spotify.mobius.First
import com.spotify.mobius.Next

fun <M, F> next(model: M, vararg effects: F): Next<M, F> = if (effects.isEmpty()) {
  Next.next(model)
} else {
  Next.next(model, setOf(*effects))
}

fun <M, F> dispatch(effect: F, vararg effects: F): Next<M, F> =
    Next.dispatch<M, F>(setOf(effect, *effects))

fun <M, F> first(model: M, vararg effects: F): First<M, F> = if (effects.isEmpty()) {
  First.first(model)
} else {
  First.first(model, setOf(*effects))
}
