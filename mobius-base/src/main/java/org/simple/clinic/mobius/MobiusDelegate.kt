package org.simple.clinic.mobius

import android.os.Bundle
import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
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

class MobiusDelegate<M : Parcelable, E, F>(
    private val events: Observable<E>,
    private val defaultModel: M,
    private val init: Init<M, F>?,
    private val update: Update<M, E, F>,
    private val effectHandler: ObservableTransformer<F, E>,
    private val modelUpdateListener: (M) -> Unit
) : Connectable<M, E> {

  @Deprecated(
      message = "This constructor is left to not break existing code. Use the version without the crashreporter parameter",
      replaceWith = ReplaceWith("MobiusDelegate(events, defaultModel, init, update, effectHandler, modelUpdateListener)")
  )
  constructor(
      events: Observable<E>,
      defaultModel: M,
      init: Init<M, F>?,
      update: Update<M, E, F>,
      effectHandler: ObservableTransformer<F, E>,
      modelUpdateListener: (M) -> Unit,
      crashReporter: CrashReporter
  ) : this(events, defaultModel, init, update, effectHandler, modelUpdateListener)

  private val modelKey = defaultModel::class.java.name
  private val viewStateKey = "ViewState_$modelKey"

  private lateinit var controller: MobiusLoop.Controller<M, E>

  private var lastKnownModel: M? = null

  private val loop by lazy(NONE) {
    val init = init ?: Init { first(defaultModel) }

    RxMobius
        .loop(
            { model: M, event: E -> update.update(model, event) },
            { effects -> effects.compose(effectHandler) }
        )
        .init(init)
  }

  @Deprecated(message = "left to not break existing code. Do not use anymore.")
  fun prepare() {
    // No-Op
  }

  private fun prepareController() {
    if(!::controller.isInitialized) {
      controller = MobiusAndroid.controller(loop, lastKnownModel ?: defaultModel)
      controller.connect(Connectables.contramap(identity(), this))
      lastKnownModel = null
    }
  }

  fun start() {
    prepareController()
    controller.start()
  }

  fun stop() {
    startControllerIfNotAlreadyRunning()
    stopAndDisconnectController()
  }

  fun onSaveInstanceState(androidViewState: Parcelable?): Parcelable {
    return Bundle().apply {
      putParcelable(viewStateKey, androidViewState)
      putParcelable(modelKey, controller.model)
    }
  }

  fun onRestoreInstanceState(parcelable: Parcelable?): Parcelable? {
    val bundle = parcelable as? Bundle
    lastKnownModel = bundle?.getParcelable(modelKey) ?: defaultModel
    return bundle?.getParcelable<Parcelable?>(viewStateKey)
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

  private fun identity(): Function<M, M> =
      Function { it }

  private fun startControllerIfNotAlreadyRunning() {
    if (controller.isRunning.not()) {
      controller.start()
    }
  }

  private fun stopAndDisconnectController() {
    controller.stop()
    controller.disconnect()
  }
}
