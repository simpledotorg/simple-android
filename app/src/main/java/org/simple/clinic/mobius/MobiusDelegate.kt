package org.simple.clinic.mobius

import android.os.Bundle
import android.os.Parcelable
import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.EventSource
import com.spotify.mobius.First
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.extras.Connectables
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.functions.Function
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import org.simple.clinic.crash.CrashReporter
import org.simple.clinic.util.unsafeLazy

class MobiusDelegate<M : Parcelable, E, F>(
    private val defaultModel: M,
    private val init: Init<M, F>,
    private val update: Update<M, E, F>,
    private val effectHandler: ObservableTransformer<F, E>,
    private val modelUpdateListener: (M) -> Unit,
    private val crashReporter: CrashReporter
) : Connectable<M, E> {
  constructor(
      defaultModel: M,
      initFunction: (M) -> First<M, F>,
      updateFunction: (M, E) -> Next<M, F>,
      effectHandler: ObservableTransformer<F, E>,
      modelUpdateListener: (M) -> Unit,
      crashReporter: CrashReporter
  ) : this(
      defaultModel,
      Init { model -> initFunction(model) },
      Update { model, event -> updateFunction(model, event) },
      effectHandler,
      modelUpdateListener,
      crashReporter
  )

  private val modelKey = defaultModel::class.java.name
  private val viewStateKey = "ViewState_$modelKey"

  private lateinit var controller: MobiusLoop.Controller<M, E>

  private var lastKnownModel: M? = null

  private val loop by unsafeLazy {
    RxMobius
        .loop(
            { model: M, event: E -> update.update(model, event) },
            { effects -> effects.compose(effectHandler) }
        )
        .init(init)
        .eventSource(eventSource)
  }

  val eventSource: EventSource<E> by unsafeLazy {
    DeferredEventSource<E>(crashReporter)
  }

  fun prepare() {
    controller = MobiusAndroid.controller(loop, lastKnownModel ?: defaultModel)
    controller.connect(Connectables.contramap(identity(), this))
    lastKnownModel = null
  }

  fun start() {
    controller.start()
  }

  fun stop() {
    controller.stop()
  }

  fun onSaveInstanceState(androidViewState: Parcelable?): Bundle {
    return Bundle().apply {
      putParcelable(viewStateKey, androidViewState)
      putParcelable(modelKey, controller.model)
    }
  }

  fun onRestoreInstanceState(bundle: Bundle?): Parcelable? {
    lastKnownModel = bundle?.getParcelable(modelKey) ?: defaultModel
    return bundle?.getParcelable(viewStateKey)
  }

  override fun connect(output: Consumer<E>): Connection<M> {
    return object : Connection<M> {
      override fun accept(value: M) {
        modelUpdateListener(value)
      }

      override fun dispose() {
        /* no-op, nothing to dispose */
      }
    }
  }

  private fun identity(): Function<M, M> =
      Function { it }
}
