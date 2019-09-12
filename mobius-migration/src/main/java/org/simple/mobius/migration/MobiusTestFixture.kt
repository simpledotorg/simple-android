package org.simple.mobius.migration

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.runners.WorkRunners
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable
import java.util.concurrent.ExecutorService

class MobiusTestFixture<M: Any, E, F>(
    events: Observable<E>,
    updateFunction: (M, E) -> Next<M, F>,
    defaultModel: M,
    renderFunction: (M) -> Unit,
    effectHandler: ObservableTransformer<F, E>,
    executorService: ExecutorService
) {
  private val disposable: Disposable
  private val controller: MobiusLoop.Controller<M, E>

  init {
    val eventSource = ImmediateEventSource<E>()
    disposable = events
        .subscribe(eventSource::notifyEvent)

    val update = Update<M, E, F> { model, event -> updateFunction(model, event) }
    val workRunner = WorkRunners.from(executorService)

    val loop = RxMobius
        .loop(update, effectHandler)
        .eventSource(eventSource)
        .eventRunner { workRunner }
        .effectRunner { workRunner }

    controller = Mobius.controller(loop, defaultModel, workRunner)

    with(controller) {
      connect(createViewConnectable(renderFunction))
      start()
    }
  }

  fun dispose() {
    disposable.dispose()
  }

  private fun createViewConnectable(renderFunction: (M) -> Unit): Connectable<M, E> {
    return Connectable {
      object : Connection<M> {
        override fun accept(value: M) {
          renderFunction(value)
        }

        override fun dispose() {
          /* nothing to dispose */
        }
      }
    }
  }

  val model: M
    get() = controller.model
}
