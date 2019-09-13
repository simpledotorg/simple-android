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
import java.util.concurrent.atomic.AtomicReference

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
  private val lastKnownEffectReference = AtomicReference<F>()

  internal val lastKnownEffect: F?
    get() = lastKnownEffectReference.get()

  val model: M
    get() = controller.model

  init {
    val immediateWorkRunner = WorkRunners.from(MoreExecutors.newDirectExecutorService())
    val eventSource = ImmediateEventSource<E>()

    val loop = createLoop(
        eventSource,
        initFunction,
        updateFunction,
        createEffectHandlerListener(effectHandler),
        immediateWorkRunner,
        requiresLogging
    )

    eventsDisposable = events.subscribe(eventSource::notifyEvent)

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

  private fun createEffectHandlerListener(
      effectHandler: ObservableTransformer<F, E>
  ): ObservableTransformer<F, E> {
    return ObservableTransformer { upstream ->
      upstream
          .doOnNext { lastKnownEffectReference.set(it) }
          .compose(effectHandler)
    }
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
