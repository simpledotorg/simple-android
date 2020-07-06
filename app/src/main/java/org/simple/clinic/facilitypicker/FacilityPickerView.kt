package org.simple.clinic.facilitypicker

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class FacilityPickerView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), FacilityPickerUi, FacilityPickerUiActions {

  init {
    inflate(context, R.layout.view_facilitypicker, this)
    context.injector<Injector>().inject(this)
  }

  @Inject
  lateinit var effectHandlerFactory: FacilityPickerEffectHandler.Factory

  private val events: Observable<FacilityPickerEvent> by unsafeLazy {
    Observable
        .never<FacilityPickerEvent>()
        .compose(ReportAnalyticsEvents())
        .cast<FacilityPickerEvent>()
  }

  private val delegate: MobiusDelegate<FacilityPickerModel, FacilityPickerEvent, FacilityPickerEffect> by unsafeLazy {
    val uiRenderer = FacilityPickerUiRenderer(this)

    MobiusDelegate.forView(
        events = events,
        defaultModel = FacilityPickerModel.create(),
        update = FacilityPickerUpdate(),
        effectHandler = effectHandlerFactory.inject(this).build(),
        init = FacilityPickerInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  interface Injector {
    fun inject(target: FacilityPickerView)
  }
}
