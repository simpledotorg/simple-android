package org.simple.clinic.removeoverdueappointment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.RemoveAppointmentReason
import org.simple.clinic.contactpatient.views.RemoveAppointmentReasonItem
import org.simple.clinic.databinding.RemoveappointmentReasonitemBinding
import org.simple.clinic.databinding.ScreenRemoveOverdueAppointmentBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.ItemAdapter
import java.util.UUID
import javax.inject.Inject

class RemoveOverdueAppointmentScreen : BaseScreen<
    RemoveOverdueAppointmentScreen.Key,
    ScreenRemoveOverdueAppointmentBinding,
    RemoveOverdueModel,
    RemoveOverdueEvent,
    RemoveOverdueEffect>(), RemoveOverdueUi, RemoveOverdueUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: RemoveOverdueEffectHandler.Factory

  private val removalReasonsAdapter = ItemAdapter(
      diffCallback = RemoveAppointmentReasonItem.DiffCallback(),
      bindings = mapOf(
          R.layout.removeappointment_reasonitem to { layoutInflater, parent ->
            RemoveappointmentReasonitemBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val doneButton
    get() = binding.removeAppointmentDone

  private val toolbar
    get() = binding.removeAppointmentToolbar

  private val removalReasonsRecyclerView
    get() = binding.removalReasonsRecyclerView

  override fun defaultModel() = RemoveOverdueModel.create(screenKey.appointmentId, screenKey.patientId)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRemoveOverdueAppointmentBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = RemoveOverdueUiRenderer(this)

  override fun events() = Observable.merge(
      removeAppointmentReasonClicks(),
      toolbarNavigationClicks(),
      doneClicks()
  ).compose(ReportAnalyticsEvents())
      .cast<RemoveOverdueEvent>()

  override fun createUpdate() = RemoveOverdueUpdate()

  override fun createEffectHandler() = effectHandlerFactory
      .create(this)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    removalReasonsRecyclerView.adapter = removalReasonsAdapter
  }

  override fun renderAppointmentRemoveReasons(reasons: List<RemoveAppointmentReason>, selectedReason: RemoveAppointmentReason?) {
    removalReasonsAdapter.submitList(RemoveAppointmentReasonItem.from(reasons, selectedReason))
  }

  override fun disableDoneButton() {
    doneButton.isEnabled = false
  }

  override fun enableDoneButton() {
    doneButton.isEnabled = true
  }

  override fun goBack() {
    router.pop()
  }

  private fun removeAppointmentReasonClicks(): Observable<RemoveOverdueEvent> {
    return removalReasonsAdapter
        .itemEvents
        .ofType<RemoveAppointmentReasonItem.Event.Clicked>()
        .map { it.reason }
        .map(::RemoveAppointmentReasonSelected)
  }

  private fun toolbarNavigationClicks(): Observable<RemoveOverdueEvent> {
    return toolbar
        .navigationClicks()
        .map { CloseClicked }
  }

  private fun doneClicks(): Observable<RemoveOverdueEvent> {
    return doneButton
        .clicks()
        .map { DoneClicked }
  }

  @Parcelize
  data class Key(val appointmentId: UUID, val patientId: UUID) : ScreenKey() {

    override val analyticsName = "Remove Overdue Appointment Screen"

    override fun instantiateFragment() = RemoveOverdueAppointmentScreen()
  }

  interface Injector {

    fun inject(target: RemoveOverdueAppointmentScreen)
  }
}
