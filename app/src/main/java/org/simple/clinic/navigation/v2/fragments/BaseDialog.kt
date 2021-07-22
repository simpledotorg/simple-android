package org.simple.clinic.navigation.v2.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.DialogFragment
import com.spotify.mobius.EventSource
import com.spotify.mobius.Init
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.mobius.eventSources
import org.simple.clinic.mobius.first
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.util.unsafeLazy

abstract class BaseDialog<K : ScreenKey, M : Parcelable, E, F, V> : DialogFragment() {

  companion object {
    private const val KEY_MODEL = "org.simple.clinic.navigation.v2.fragments.BaseScreen.KEY_MODEL"
  }

  private val loop: MobiusLoop.Builder<M, E, F> by unsafeLazy {
    RxMobius
        .loop(createUpdate()::update, createEffectHandler())
        .eventSources(additionalEventSources())
  }

  private val controller: MobiusLoop.Controller<M, E> by unsafeLazy {
    MobiusAndroid.controller(loop, defaultModel(), createInit())
  }

  protected val screenKey by unsafeLazy { ScreenKey.key<K>(this) }

  abstract fun defaultModel(): M

  abstract fun createDialog(savedInstanceState: Bundle?): Dialog

  open fun uiRenderer(): ViewRenderer<M> = NoopViewRenderer()

  open fun events(): Observable<E> = Observable.never()

  open fun createUpdate(): Update<M, E, F> = Update { _, _ -> noChange() }

  open fun createInit(): Init<M, F> = Init { model -> first(model) }

  open fun createEffectHandler(): ObservableTransformer<F, E> = ObservableTransformer { Observable.never() }

  open fun additionalEventSources(): List<EventSource<E>> = emptyList()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return createDialog(savedInstanceState)
  }

  private fun backPressed() {
    requireActivity().onBackPressed()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

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
  }

  override fun onCancel(dialog: DialogInterface) {
    backPressed()
    super.onCancel(dialog)
  }
}
