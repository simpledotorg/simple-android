package org.simple.mobius.migration

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.runners.WorkRunners
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.ExecutorService

class MobiusTestFixture<M: Any, E, F>(
    defaultModel: M,
    updateFunction: (M, E) -> Next<M, F>,
    events: Observable<E>,
    executorService: ExecutorService
) {
  private val disposable: Disposable
  private val controller: MobiusLoop.Controller<M, E>

  init {
    val eventSource = ImmediateEventSource<E>()
    disposable = events
        .subscribe(eventSource::notifyEvent)

    val update = Update<M, E, F> { model, event -> updateFunction(model, event) }
    val effectHandler = createEffectHandler()
    val workRunner = WorkRunners.from(executorService)

    val loop = Mobius
        .loop(update, effectHandler)
        .eventSource(eventSource)
        .eventRunner { workRunner }
        .effectRunner { workRunner }

    controller = Mobius.controller(loop, defaultModel, workRunner)

    with(controller) {
      connect(createViewConnectable())
      start()
    }
  }

  private fun createEffectHandler() = Connectable<F, E> {
    object : Connection<F> {
      override fun accept(value: F) {
        /* no-op */
      }

      override fun dispose() {
        /* no-op */
      }
    }
  }

  private fun createViewConnectable(): Connectable<M, E> {
    return Connectable {
      object : Connection<M> {
        override fun accept(value: M) {
          /* no-op */
        }

        override fun dispose() {
          /* no-op */
        }
      }
    }
  }

  val model: M
    get() = controller.model
}
