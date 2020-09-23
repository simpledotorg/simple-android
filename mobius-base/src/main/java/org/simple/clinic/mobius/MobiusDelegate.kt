package org.simple.clinic.mobius

import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.EventSource
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.extras.Connectables
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.functions.Function
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.platform.crash.CrashReporter
import kotlin.LazyThreadSafetyMode.NONE

class MobiusDelegate<M : Parcelable, E, F> private constructor(
    private val events: Observable<E>,
    private val defaultModel: M,
    private val init: Init<M, F>,
    private val update: Update<M, E, F>,
    private val effectHandler: ObservableTransformer<F, E>,
    private val modelUpdateListener: (M) -> Unit,
    private val savedStateHandle: SavedStateHandle<M>,
    private val additionalEventSources: List<EventSource<E>>
) : Connectable<M, E> {

  companion object {
    fun <M : Parcelable, E, F> forView(
        events: Observable<E>,
        defaultModel: M,
        update: Update<M, E, F>,
        effectHandler: ObservableTransformer<F, E>,
        init: Init<M, F> = Init { first(defaultModel) },
        modelUpdateListener: (M) -> Unit = {},
        additionalEventSources: List<EventSource<E>> = emptyList()
    ): MobiusDelegate<M, E, F> {
      return MobiusDelegate(
          events = events,
          defaultModel = defaultModel,
          init = init,
          update = update,
          effectHandler = effectHandler,
          modelUpdateListener = modelUpdateListener,
          savedStateHandle = ViewSavedStateHandle(defaultModel::class.java.name),
          additionalEventSources = additionalEventSources
      )
    }

    fun <M : Parcelable, E, F> forActivity(
        events: Observable<E>,
        defaultModel: M,
        update: Update<M, E, F>,
        effectHandler: ObservableTransformer<F, E>,
        init: Init<M, F> = Init { first(defaultModel) },
        modelUpdateListener: (M) -> Unit = {},
        additionalEventSources: List<EventSource<E>> = emptyList()
    ): MobiusDelegate<M, E, F> {
      return MobiusDelegate(
          events = events,
          defaultModel = defaultModel,
          init = init,
          update = update,
          effectHandler = effectHandler,
          modelUpdateListener = modelUpdateListener,
          savedStateHandle = ActivitySavedStateHandle(defaultModel::class.java.name),
          additionalEventSources = additionalEventSources
      )
    }
  }

  @Deprecated(
      message = "This constructor is left to not break existing code. Use the version without the crashreporter parameter",
      replaceWith = ReplaceWith("MobiusDelegate.forView(events, defaultModel, init, update, effectHandler, modelUpdateListener)")
  )
  constructor(
      events: Observable<E>,
      defaultModel: M,
      init: Init<M, F>?,
      update: Update<M, E, F>,
      effectHandler: ObservableTransformer<F, E>,
      modelUpdateListener: (M) -> Unit,
      @Suppress("UNUSED_PARAMETER") crashReporter: CrashReporter
  ) : this(
      events = events,
      defaultModel = defaultModel,
      init = init ?: Init { first(defaultModel) },
      update = update,
      effectHandler = effectHandler,
      modelUpdateListener = modelUpdateListener,
      savedStateHandle = ViewSavedStateHandle(defaultModel::class.java.name),
      additionalEventSources = emptyList()
  )

  private val controller: MobiusLoop.Controller<M, E> by lazy(NONE) {
    MobiusAndroid.controller(loop, lastKnownModel ?: defaultModel)
  }

  private var lastKnownModel: M? = null

  private val loop by lazy(NONE) {
    RxMobius
        .loop(
            { model: M, event: E -> update.update(model, event) },
            { effects -> effects.compose(effectHandler) }
        )
        .init(init)
        .eventSources(additionalEventSources)
  }

  val currentModel: M
    get() = controller.model

  @Deprecated(message = "left to not break existing code. Do not use anymore.")
  fun prepare() {
    // No-Op
  }

  fun start() {
    controller.connect(Connectables.contramap(identity(), this))
    controller.start()
    lastKnownModel = null
  }

  fun stop() {
    if (controller.isRunning) {
      controller.stop()
    }
    controller.disconnect()
  }

  fun onSaveInstanceState(androidViewState: Parcelable?): Parcelable {
    return savedStateHandle.save(androidViewState, controller.model)
  }

  fun onRestoreInstanceState(parcelable: Parcelable?): Parcelable? {
    val restored: Pair<M, Parcelable?>? = savedStateHandle.restore(parcelable)

    return if (restored != null) {
      lastKnownModel = restored.first
      restored.second
    } else {
      // This is an activity that is being created, nothing to restore here
      null
    }
  }

  override fun connect(output: Consumer<E>): Connection<M> {
    val eventsDisposable = events.subscribe(output::accept)

    return object : Connection<M> {
      override fun accept(value: M) {
        modelUpdateListener(value)
      }

      override fun dispose() {
        eventsDisposable.dispose()
      }
    }
  }

  private fun identity(): Function<M, M> = Function { it }
}
