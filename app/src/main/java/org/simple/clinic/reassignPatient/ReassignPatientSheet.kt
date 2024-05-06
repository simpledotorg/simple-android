package org.simple.clinic.reassignPatient

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.spotify.mobius.functions.Consumer
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.SheetReassignPatientBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
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

  private val assignedFacilityName
    get() = binding.assignedFacilityTextView

  override fun defaultModel() = ReassignPatientModel.create(
      patientUuid = screenKey.patientId,
  )

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) = SheetReassignPatientBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = ReassignPatientUiRenderer(this)

  override fun createUpdate() = ReassignPatientUpdate()

  override fun createInit() = ReassignPatientInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<ReassignPatientViewEffect>) =
      effectHandlerFactory.create(viewEffectsConsumer = viewEffectsConsumer).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun renderAssignedFacilityName(facilityName: String) {
    assignedFacilityName.text = facilityName
  }

  override fun closeSheet() {
  }

  override fun openSelectFacilitySheet() {}

  @Parcelize
  data class Key(
      val patientId: UUID,
  ) : ScreenKey() {

    override val analyticsName = "Reassign Patient Sheet"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = ReassignPatientSheet()
  }

  interface Injector {
    fun inject(target: ReassignPatientSheet)
  }
}
