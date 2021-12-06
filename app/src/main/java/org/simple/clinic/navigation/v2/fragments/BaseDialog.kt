package org.simple.clinic.navigation.v2.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.viewbinding.ViewBinding
import com.spotify.mobius.EventSource
import com.spotify.mobius.Init
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusLoopViewModel
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable
import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.mobius.eventSources
import org.simple.clinic.mobius.first
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.util.unsafeLazy

abstract class BaseDialog<K : ScreenKey, B : ViewBinding, M : Parcelable, E, F, V> : DialogFragment() {

  companion object {
    private const val KEY_MODEL = "org.simple.clinic.navigation.v2.fragments.BaseScreen.KEY_MODEL"
  }

  private lateinit var viewModel: MobiusLoopViewModel<M, E, F, V>
  private lateinit var eventsDisposable: Disposable

  protected val screenKey by unsafeLazy { ScreenKey.key<K>(this) }

  private var _binding: B? = null

  protected val binding: B
    get() = _binding!!

  val screenName: String
    get() = screenKey.analyticsName

  abstract fun defaultModel(): M

  abstract fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): B

  open fun uiRenderer(): ViewRenderer<M> = NoopViewRenderer()

  open fun viewEffectHandler(): ViewEffectsHandler<V> = NoopViewEffectsHandler()

  open fun events(): Observable<E> = Observable.never()

  open fun createUpdate(): Update<M, E, F> = Update { _, _ -> noChange() }

  open fun createInit(): Init<M, F> = Init { model -> first(model) }

  open fun createEffectHandler(viewEffectsConsumer: Consumer<V>): ObservableTransformer<F, E> = ObservableTransformer { Observable.never() }

  open fun additionalEventSources(): List<EventSource<E>> = emptyList()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    _binding = bindView(inflater, container)

    return _binding?.root
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    eventsDisposable = events().subscribe { viewModel.dispatchEvent(it!!) }

    val uiRenderer = uiRenderer()
    viewModel.models.observe(viewLifecycleOwner, uiRenderer::render)

    val viewEffectHandler = viewEffectHandler()
    viewModel.viewEffects.setObserver(
        viewLifecycleOwner,
        { liveViewEffect -> viewEffectHandler.handle(liveViewEffect) },
        { pausedViewEffects -> pausedViewEffects.forEach(viewEffectHandler::handle) }
    )
  }

  override fun onDestroyView() {
    super.onDestroyView()
    eventsDisposable.dispose()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (::viewModel.isInitialized) {
      outState.putParcelable(KEY_MODEL, viewModel.model)
    }
  }

  override fun onCancel(dialog: DialogInterface) {
    backPressed()
    super.onCancel(dialog)
  }

  private fun backPressed() {
    requireActivity().onBackPressed()
  }
}
