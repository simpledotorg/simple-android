package org.simple.clinic.scheduleappointment.facilityselection

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenSelectFacilityBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class FacilitySelectionScreen :
    BaseScreen<
        FacilitySelectionScreen.Key,
        ScreenSelectFacilityBinding,
        FacilitySelectionModel,
        FacilitySelectionEvent,
        FacilitySelectionEffect,
        Unit>(),
    FacilitySelectionUi,
    FacilitySelectionUiActions {

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var effectHandlerFactory: FacilitySelectionEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val facilityPickerView
    get() = binding.facilityPickerView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    requireContext().injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    facilityPickerView.backClicked = router::pop
  }

  private fun facilityClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      facilityPickerView.facilitySelectedCallback = { emitter.onNext(FacilitySelected(it)) }
    }
  }

  override fun sendSelectedFacility(selectedFacility: Facility) {
    router.popWithResult(Succeeded(SelectedFacility(selectedFacility)))
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
  ): ScreenSelectFacilityBinding {
    return ScreenSelectFacilityBinding.inflate(layoutInflater, container, false)
  }

  @Parcelize
  class Key(
      override val analyticsName: String = "Select Facility"
  ) : ScreenKey() {

    override fun instantiateFragment() = FacilitySelectionScreen()
  }

  interface Injector {
    fun inject(target: FacilitySelectionScreen)
  }

  @Parcelize
  data class SelectedFacility(val facility: Facility) : Parcelable
}
