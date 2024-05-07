package org.simple.clinic.reassignPatient

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetReassignPatientBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionScreen
import org.simple.clinic.util.setFragmentResultListener
import java.util.UUID
import javax.inject.Inject

class ReassignPatientSheet : BaseBottomSheet<
    ReassignPatientSheet.Key,
    SheetReassignPatientBinding,
    ReassignPatientModel,
    ReassignPatientEvent,
    ReassignPatientEffect,
    ReassignPatientViewEffect>(),
    ReassignPatientUi,
    ReassignPatientUiActions {

  @Inject
  lateinit var effectHandlerFactory: ReassignPatientEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val additionalEvents = DeferredEventSource<ReassignPatientEvent>()

  private val assignedFacilityName
    get() = binding.assignedFacilityTextView

  private val notNowButton
    get() = binding.notNowButton

  private val changeButton
    get() = binding.changeButton

  override fun defaultModel() = ReassignPatientModel.create(
      patientUuid = screenKey.patientId,
  )

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetReassignPatientBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = ReassignPatientUiRenderer(this)

  override fun createUpdate() = ReassignPatientUpdate()

  override fun createInit() = ReassignPatientInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<ReassignPatientViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun viewEffectsHandler() = ReassignPatientViewEffectHandler(this)

  override fun additionalEventSources() = listOf(
      additionalEvents
  )
  
  override fun events() = Observable
      .mergeArray(
          notNowClicks(),
          changeClicks(),
      )
      .compose(ReportAnalyticsEvents())
      .cast<ReassignPatientEvent>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setFragmentResultListener(ScreenRequest.SelectFacility) { requestKey, result ->
      if (result is Succeeded) {
        handleScreenResult(requestKey, result)
      }
    }
  }

  private fun handleScreenResult(requestKey: Parcelable, result: Succeeded) {
    when (requestKey) {
      is ScreenRequest.SelectFacility -> {
        val selectedFacility = (result.result as FacilitySelectionScreen.SelectedFacility).facility
        additionalEvents.notify(NewAssignedFacilitySelected(selectedFacility))
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  private fun notNowClicks() = notNowButton.clicks().map { NotNowClicked }

  private fun changeClicks() = changeButton.clicks().map { ChangeClicked }

  override fun renderAssignedFacilityName(facilityName: String) {
    assignedFacilityName.text = facilityName
  }

  override fun closeSheet(sheetClosedFrom: ReassignPatientSheetClosedFrom) {
  }

  override fun openSelectFacilitySheet() {
    router.pushExpectingResult(ScreenRequest.SelectFacility, FacilitySelectionScreen.Key())
  }

  @Parcelize
  data class Key(
      val patientId: UUID,
      val sheetOpenedFrom: ReassignPatientSheetOpenedFrom
  ) : ScreenKey() {

    override val analyticsName = "Reassign Patient Sheet"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = ReassignPatientSheet()
  }

  sealed class ScreenRequest : Parcelable {

    @Parcelize
    data object SelectFacility : ScreenRequest()
  }

  interface Injector {
    fun inject(target: ReassignPatientSheet)
  }
}
