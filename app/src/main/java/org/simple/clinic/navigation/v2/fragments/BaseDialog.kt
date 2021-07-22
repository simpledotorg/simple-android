package org.simple.clinic.navigation.v2.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.spotify.mobius.EventSource
import com.spotify.mobius.Init
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusLoopViewModel
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.mobius.eventSources
import org.simple.clinic.mobius.first
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.util.unsafeLazy

abstract class BaseDialog<K : ScreenKey, M : Parcelable, E, F, V> : DialogFragment() {

  companion object {
    private const val KEY_MODEL = "org.simple.clinic.navigation.v2.fragments.BaseScreen.KEY_MODEL"
  }

  private lateinit var viewModel: MobiusLoopViewModel<M, E, F, V>

  protected val screenKey by unsafeLazy { ScreenKey.key<K>(this) }

  abstract fun defaultModel(): M

  abstract fun createDialog(savedInstanceState: Bundle?): Dialog

  open fun uiRenderer(): ViewRenderer<M> = NoopViewRenderer()

  open fun viewEffectHandler(): ViewEffectsHandler<V> = NoopViewEffectsHandler()

  open fun events(): Observable<E> = Observable.never()

  open fun createUpdate(): Update<M, E, F> = Update { _, _ -> noChange() }

  open fun createInit(): Init<M, F> = Init { model -> first(model) }

  open fun createEffectHandler(viewEffectsConsumer: Consumer<V>): ObservableTransformer<F, E> = ObservableTransformer { Observable.never() }

  open fun additionalEventSources(): List<EventSource<E>> = emptyList()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return createDialog(savedInstanceState)
  }

  private fun backPressed() {
    requireActivity().onBackPressed()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val startModel = savedInstanceState?.getParcelable(KEY_MODEL) ?: defaultModel()

    viewModel = ViewModelProvider(viewModelStore, object : ViewModelProvider.Factory {
      private fun loop(viewEffectsConsumer: Consumer<V>) = RxMobius
          .loop(createUpdate(), createEffectHandler(viewEffectsConsumer))
          .eventSources(additionalEventSources())

      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MobiusLoopViewModel.create<M, E, F, V>(
            ::loop,
            startModel,
            createInit()
        ) as T
      }
    }).get()

    val uiRenderer = uiRenderer()
    viewModel.models.observe(viewLifecycleOwner, uiRenderer::render)

    val viewEffectHandler = viewEffectHandler()
    viewModel.viewEffects.setObserver(
        viewLifecycleOwner,
        { liveViewEffect -> viewEffectHandler.handle(liveViewEffect) },
        { pausedViewEffects -> pausedViewEffects.forEach(viewEffectHandler::handle) }
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(KEY_MODEL, viewModel.model)
  }

  override fun onCancel(dialog: DialogInterface) {
    backPressed()
    super.onCancel(dialog)
  }
}
