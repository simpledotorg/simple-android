package org.simple.clinic.mobius

import com.spotify.mobius.First
import com.spotify.mobius.Init
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch

fun <M, F> ((M) -> First<M, F>).toInit(): Init<M, F> =
    Init { this.invoke(it) }

fun <M, F> justEffect(effect: F): Next<M, F> =
    dispatch<M, F>(setOf(effect))
