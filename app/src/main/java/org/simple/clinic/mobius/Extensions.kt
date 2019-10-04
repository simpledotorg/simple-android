package org.simple.clinic.mobius

import com.spotify.mobius.First
import com.spotify.mobius.Init

fun <M, F> ((M) -> First<M, F>).toInit(): Init<M, F> =
    Init { this.invoke(it) }
