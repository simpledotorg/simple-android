package org.simple.clinic.mobius

import com.spotify.mobius.EventSource
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
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

fun <M, E, F> MobiusLoop.Builder<M, E, F>.eventSources(eventsSources: List<EventSource<E>>): MobiusLoop.Builder<M, E, F> {
  return when {
    eventsSources.isEmpty() -> this
    eventsSources.size == 1 -> eventSource(eventsSources.first())
    else -> eventSources(eventsSources.first(), *eventsSources.subList(1, eventsSources.size).toTypedArray())
  }
}
