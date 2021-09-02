package org.simple.clinic.scheduleappointment.facilityselection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ActivitySelectFacilityBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class FacilitySelectionActivity :
    BaseScreen<
        FacilitySelectionActivity.Key,
        ActivitySelectFacilityBinding,
        FacilitySelectionModel,
        FacilitySelectionEvent,
        FacilitySelectionEffect,
        Unit>(),
    FacilitySelectionUi,
    FacilitySelectionUiActions {

  companion object {
    const val EXTRA_SELECTED_FACILITY = "selected_facility"

    fun selectedFacility(data: Intent): Facility {
      return data.getParcelableExtra(EXTRA_SELECTED_FACILITY)!!
    }
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: FacilitySelectionEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private lateinit var binding: ActivitySelectFacilityBinding

  private val facilityPickerView
    get() = binding.facilityPickerView

  private val events by unsafeLazy {
    facilityClicks()
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = FacilitySelectionUiRenderer(this)

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = FacilitySelectionModel(),
        update = FacilitySelectionUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = FacilitySelectionInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    requireContext().injector<Injector>().inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivitySelectFacilityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    facilityPickerView.backClicked = this@FacilitySelectionActivity::finish

    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  private fun facilityClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(FacilitySelected(it)) }
    }
  }

  override fun sendSelectedFacility(selectedFacility: Facility) {
    val intent = Intent()
    intent.putExtra(EXTRA_SELECTED_FACILITY, selectedFacility)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  override fun defaultModel() = FacilitySelectionModel()

  override fun uiRenderer() = FacilitySelectionUiRenderer(this)

  override fun events(): Observable<FacilitySelectionEvent> {
    return facilityClicks()
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createUpdate() = FacilitySelectionUpdate()

  override fun createInit() = FacilitySelectionInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this).build()

  override fun bindView(
      layoutInflater: LayoutInflater,
      container: ViewGroup?
  ): ActivitySelectFacilityBinding {
    return ActivitySelectFacilityBinding.inflate(layoutInflater, container, false)
  }

  @Parcelize
  class Key(
      override val analyticsName: String = "Select Facility"
  ) : ScreenKey() {

    override fun instantiateFragment() = FacilitySelectionActivity()
  }

  interface Injector {
    fun inject(target: FacilitySelectionActivity)
  }
}
