package org.simple.mobius.migration

import com.spotify.mobius.EventSource
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer

internal class ImmediateEventSource<E> : EventSource<E> {
  private var consumer: Consumer<E>? = null

  override fun subscribe(eventConsumer: Consumer<E>): Disposable {
    consumer = eventConsumer
    return Disposable {
      consumer = null
    }
  }

  fun notifyEvent(e: E) {
    requireNotNull(consumer) { "'consumer' cannot be null" }
    consumer!!.accept(e)
  }
}
