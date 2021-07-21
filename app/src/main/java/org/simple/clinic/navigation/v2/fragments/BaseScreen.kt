package org.simple.clinic.navigation.v2.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.viewbinding.ViewBinding
import com.spotify.mobius.EventSource
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.android.MobiusLoopViewModel
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.mobius.eventSources
import org.simple.clinic.mobius.first
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.util.unsafeLazy

abstract class BaseScreen<K : ScreenKey, B : ViewBinding, M : Parcelable, E, F, V> : Fragment() {

  companion object {
    private const val KEY_MODEL = "org.simple.clinic.navigation.v2.fragments.BaseScreen.KEY_MODEL"
  }

  private lateinit var _viewModel: MobiusLoopViewModel<M, E, F, V>

  private val loop: MobiusLoop.Builder<M, E, F> by unsafeLazy {
    RxMobius
        .loop(createUpdate()::update, createEffectHandler())
        .eventSources(additionalEventSources())
  }

  private val controller: MobiusLoop.Controller<M, E> by unsafeLazy {
    MobiusAndroid.controller(loop, defaultModel(), createInit())
  }

  protected val screenKey by unsafeLazy { ScreenKey.key<K>(this) }

  private var _binding: B? = null

  protected val binding: B
    get() = _binding!!

  val currentModel
    get() = controller.model

  abstract fun defaultModel(): M

  abstract fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): B

  open fun uiRenderer(): ViewRenderer<M> = NoopViewRenderer()

  open fun events(): Observable<E> = Observable.never()

  open fun createUpdate(): Update<M, E, F> = Update { _, _ -> noChange() }

  open fun createInit(): Init<M, F> = Init { model -> first(model) }

  open fun createEffectHandler(): ObservableTransformer<F, E> = ObservableTransformer { Observable.never() }

  open fun additionalEventSources(): List<EventSource<E>> = emptyList()

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    _binding = bindView(inflater, container)

    return _binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val startModel = savedInstanceState?.getParcelable(KEY_MODEL) ?: defaultModel()

    _viewModel = ViewModelProvider(viewModelStore, object : ViewModelProvider.Factory {
      private fun loop(viewEffectsConsumer: Consumer<V>) = RxMobius
          .loop(createUpdate(), createEffectHandler())
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

    val rxBridge = RxMobiusBridge(events(), uiRenderer())
    controller.connect(rxBridge)

    if (savedInstanceState != null) {
      val savedModel = savedInstanceState.getParcelable<M>(KEY_MODEL)!!
      controller.replaceModel(savedModel)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    controller.disconnect()
    _binding = null
  }

  override fun onResume() {
    super.onResume()
    controller.start()
  }

  override fun onPause() {
    super.onPause()
    controller.stop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putParcelable(KEY_MODEL, controller.model)
    outState.putParcelable(KEY_MODEL, _viewModel.model)
  }
}
