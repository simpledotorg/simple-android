package org.simple.mobius.migration

import com.google.common.util.concurrent.MoreExecutors
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.EventSource
import com.spotify.mobius.First
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.runners.WorkRunner
import com.spotify.mobius.runners.WorkRunners
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable

typealias InitFunction<M, F> = (M) -> First<M, F>
typealias UpdateFunction<M, E, F> = (M, E) -> Next<M, F>
typealias ModelUpdateListener<M> = (M) -> Unit
typealias EffectHandler<F, E> = ObservableTransformer<F, E>

class MobiusTestFixture<M: Any, E, F>(
    events: Observable<E>,
    defaultModel: M,
    initFunction: InitFunction<M, F>?,
    updateFunction: UpdateFunction<M, E, F>,
    effectHandler: EffectHandler<F, E>,
    modelUpdateListener: ModelUpdateListener<M>,
    requiresLogging: Boolean = false
) {
  private val eventsDisposable: Disposable
  private val controller: MobiusLoop.Controller<M, E>

  val model: M
    get() = controller.model

  init {
    val immediateWorkRunner = WorkRunners.from(MoreExecutors.newDirectExecutorService())
    val eventSource = ImmediateEventSource<E>()
    eventsDisposable = events.subscribe(eventSource::notifyEvent)

    val loop = createLoop(
        eventSource,
        initFunction,
        updateFunction,
        effectHandler,
        immediateWorkRunner,
        requiresLogging
    )

    controller = Mobius.controller(loop, defaultModel, immediateWorkRunner)
    with(controller) {
      connect(createModelUpdateListenerConnectable(modelUpdateListener))
      start()
    }
  }

  fun dispose() {
    eventsDisposable.dispose()
  }

  private fun createLoop(
      eventSource: EventSource<E>,
      initFunction: InitFunction<M, F>?,
      updateFunction: UpdateFunction<M, E, F>,
      effectHandlerListener: EffectHandler<F, E>,
      workRunner: WorkRunner,
      requiresLogging: Boolean
  ): MobiusLoop.Builder<M, E, F> {
    val update = Update<M, E, F> { model, event -> updateFunction(model, event) }

    return RxMobius
        .loop(update, effectHandlerListener)
        .init { model -> initFunction?.invoke(model) ?: First.first(model) }
        .eventSource(eventSource)
        .eventRunner { workRunner }
        .effectRunner { workRunner }
        .logger(if (requiresLogging) ConsoleLogger<M, E, F>() else NoopLogger())
  }

  private fun createModelUpdateListenerConnectable(
      modelUpdateListener: (M) -> Unit
  ): Connectable<M, E> {
    return Connectable {
      object : Connection<M> {
        override fun accept(value: M) {
          modelUpdateListener(value)
        }

        override fun dispose() {
          /* nothing to dispose */
        }
      }
    }
  }
}
