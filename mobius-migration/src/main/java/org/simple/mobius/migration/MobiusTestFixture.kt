package org.simple.mobius.migration

import com.google.common.util.concurrent.MoreExecutors
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.EventSource
import com.spotify.mobius.First
import com.spotify.mobius.Init
import com.spotify.mobius.Mobius
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.runners.WorkRunner
import com.spotify.mobius.runners.WorkRunners
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable

typealias ModelUpdateListener<M> = (M) -> Unit
typealias EffectHandler<F, E> = ObservableTransformer<F, E>

class MobiusTestFixture<M : Any, E, F>(
  events: Observable<E>,
  defaultModel: M,
  init: Init<M, F>?,
  update: Update<M, E, F>,
  effectHandler: EffectHandler<F, E>,
  modelUpdateListener: ModelUpdateListener<M>,
  requiresLogging: Boolean = false,
  additionalEventSources: List<EventSource<E>> = emptyList()
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
        spyingUpdate(update, modelUpdateListener),
        effectHandler,
        immediateWorkRunner,
        requiresLogging,
        additionalEventSources
    )

    controller = Mobius.controller(loop,
        defaultModel,
        spyingInit(init, modelUpdateListener),
        immediateWorkRunner)
  }

  fun start() {
    with(controller) {
      connect(createNoOpConnectable())
      start()
    }
  }

  fun dispose() {
    eventsDisposable.dispose()
  }

  private fun createLoop(
    eventSource: EventSource<E>,
    update: Update<M, E, F>,
    effectHandlerListener: EffectHandler<F, E>,
    workRunner: WorkRunner,
    requiresLogging: Boolean,
    additionalEventSources: List<EventSource<E>>
  ): MobiusLoop.Builder<M, E, F> {
    return RxMobius
        .loop(update, effectHandlerListener)
        .eventSources(eventSource, *additionalEventSources.toTypedArray())
        .eventRunner { workRunner }
        .effectRunner { workRunner }
        .logger(if (requiresLogging) ConsoleLogger<M, E, F>() else NoopLogger())
  }

  private fun spyingInit(
    init: Init<M, F>?,
    modelUpdateListener: ModelUpdateListener<M>
  ): Init<M, F> {
    return Init { model ->
      (init?.init(model) ?: First.first(model)).also { first ->
        modelUpdateListener(first.model())
      }
    }
  }

  private fun spyingUpdate(
    update: Update<M, E, F>,
    modelUpdateListener: ModelUpdateListener<M>
  ): Update<M, E, F> {
    return Update { model, event ->
      update.update(model, event).also { next ->
        if (next.hasModel()) {
          modelUpdateListener(next.modelUnsafe())
        }
      }
    }
  }

  private fun createNoOpConnectable(): Connectable<M, E> {
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
}
