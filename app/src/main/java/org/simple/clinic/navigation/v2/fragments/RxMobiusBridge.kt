package org.simple.clinic.navigation.v2.fragments

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import org.simple.clinic.mobius.ViewRenderer

class RxMobiusBridge<M, E, R : ViewRenderer<M>>(
    private val eventStream: Observable<E>,
    private val uiRenderer: R
) : Connectable<M, E> {

  override fun connect(eventConsumer: Consumer<E>): Connection<M> {
    val eventsDisposable = eventStream.subscribe(eventConsumer::accept)

    return object : Connection<M> {

      override fun dispose() {
        eventsDisposable.dispose()
      }

      override fun accept(model: M) {
        uiRenderer.render(model)
      }
    }
  }
}
