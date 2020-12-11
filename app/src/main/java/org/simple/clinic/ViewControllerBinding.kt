package org.simple.clinic

import android.view.View
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.plumbing.BaseUiChangeProducer
import org.simple.clinic.plumbing.BaseUiStateProducer
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenRestored

class ViewControllerBinding<E, S, T>(
    private val uiStateProducer: BaseUiStateProducer<E, S>,
    private val uiChangeProducer: BaseUiChangeProducer<S, T>
) : View.OnAttachStateChangeListener {

  companion object {

    fun <E, S, T> bindToView(
        view: View,
        uiStateProducer: BaseUiStateProducer<E, S>,
        uiChangeProducer: BaseUiChangeProducer<S, T>
    ): ViewControllerBinding<E, S, T> {
      val viewControllerBinding = ViewControllerBinding(
          uiStateProducer = uiStateProducer,
          uiChangeProducer = uiChangeProducer
      )

      view.addOnAttachStateChangeListener(viewControllerBinding)

      return viewControllerBinding
    }
  }

  private var disposable: Disposable? = null

  private val uiEvents: PublishSubject<E> = PublishSubject.create()

  @Suppress("UNCHECKED_CAST")
  override fun onViewAttachedToWindow(v: View) {
    val stateSubject = uiStateProducer.states

    disposable = uiEvents
        .compose(uiStateProducer)
        .share()
        .doOnNext { state -> stateSubject.onNext(state) }
        .compose(uiChangeProducer)
        .subscribe { uiChange -> uiChange(v as T) }

    val screenEvent = if (stateSubject.value == null) ScreenCreated() else ScreenRestored
    uiEvents.onNext(screenEvent as E)
  }

  override fun onViewDetachedFromWindow(v: View) {
    disposable!!.dispose()
    disposable = null
    v.removeOnAttachStateChangeListener(this)
  }

  fun onEvent(event: E) {
    uiEvents.onNext(event)
  }
}
