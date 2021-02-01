package org.simple.clinic.facility.change.confirm

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetConfirmFacilityChangeBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class ConfirmFacilityChangeSheet :
    BaseBottomSheet<
        ConfirmFacilityChangeSheet.Key,
        SheetConfirmFacilityChangeBinding,
        ConfirmFacilityChangeModel,
        ConfirmFacilityChangeEvent,
        ConfirmFacilityChangeEffect>(),
    ConfirmFacilityChangeUiActions {

  companion object {
    fun wasFacilityChanged(result: Succeeded) = result.result is Confirmed
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var effectHandlerFactory: ConfirmFacilityChangeEffectHandler.Factory

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  override fun defaultModel(): ConfirmFacilityChangeModel {
    return ConfirmFacilityChangeModel.create()
  }

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?): SheetConfirmFacilityChangeBinding {
    return SheetConfirmFacilityChangeBinding.inflate(inflater, container, false)
  }

  override fun events() = positiveButtonClicks()
      .compose(ReportAnalyticsEvents())
      .cast<ConfirmFacilityChangeEvent>()

  override fun createUpdate() = ConfirmFacilityChangeUpdate()

  override fun createInit() = ConfirmFacilityChangeInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  private val selectedFacility: Facility by unsafeLazy { screenKey.facility }

  private val facilityName
    get() = binding.facilityName

  private val cancelButton
    get() = binding.cancelButton

  private val yesButton
    get() = binding.yesButton

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    facilityName.text = getString(R.string.confirmfacilitychange_facility_name, selectedFacility.name)
    cancelButton.setOnClickListener {
      closeSheetAfterCancel()
    }
  }

  private fun closeSheetAfterCancel() {
    router.pop()
  }

  private fun positiveButtonClicks(): Observable<UiEvent> =
      yesButton.clicks().map { FacilityChangeConfirmed(selectedFacility) }

  override fun closeSheet() {
    router.popWithResult(Succeeded(Confirmed))
  }

  @Parcelize
  object Confirmed : Parcelable

  interface Injector {
    fun inject(target: ConfirmFacilityChangeSheet)
  }

  @Parcelize
  data class Key(val facility: Facility) : ScreenKey() {

    override val analyticsName = "Confirm Facility Change"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = ConfirmFacilityChangeSheet()
  }
}
