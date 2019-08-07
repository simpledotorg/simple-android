package org.simple.clinic.home.overdue

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_overdue.view.*
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.home.overdue.appointmentreminder.AppointmentReminderSheet
import org.simple.clinic.home.overdue.phonemask.PhoneMaskBottomSheet
import org.simple.clinic.home.overdue.removepatient.RemoveAppointmentScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.locationRectOnScreen
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID
import javax.inject.Inject

class OverdueScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: OverdueScreenController

  @Inject
  lateinit var userClock: UserClock

  private val overdueListAdapter = ItemAdapter(OverdueAppointmentRow.DiffCallback())

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)

    val screenDestroys = RxView
        .detaches(this)
        .map { ScreenDestroyed() }

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            overdueListAdapter.itemEvents
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )

    setupCardExpansionEvents(
        cardExpansionToggledStream = overdueListAdapter.itemEvents.ofType(OverdueAppointmentRow.CardExpansionToggled::class.java),
        screenDestroys = screenDestroys
    )
  }

  @SuppressLint("CheckResult")
  private fun setupCardExpansionEvents(
      cardExpansionToggledStream: Observable<OverdueAppointmentRow.CardExpansionToggled>,
      screenDestroys: Observable<ScreenDestroyed>
  ) {
    cardExpansionToggledStream
        .takeUntil(screenDestroys)
        .map { it.cardBottomWithMargin }
        .subscribe(this::scrollTillExpandedCardIsVisible)
  }

  private fun screenCreates() = Observable.just(OverdueScreenCreated())

  private fun scrollTillExpandedCardIsVisible(cardBottomWithMargin: Int) {
    val rvLocation = overdueRecyclerView.locationRectOnScreen()
    val differenceInBottoms = cardBottomWithMargin - rvLocation.bottom

    if (differenceInBottoms > 0) {
      overdueRecyclerView.smoothScrollBy(0, differenceInBottoms)
    }
  }

  fun updateList(overdueAppointments: List<OverdueAppointment>) {
    overdueListAdapter.submitList(OverdueAppointmentRow.from(overdueAppointments, userClock))
  }

  fun handleEmptyList(isEmpty: Boolean) {
    viewForEmptyList.visibleOrGone(isEmpty)
    overdueRecyclerView.visibleOrGone(isEmpty.not())
  }

  fun showAppointmentReminderSheet(appointmentUuid: UUID) {
    val intent = AppointmentReminderSheet.intent(context, appointmentUuid)
    activity.startActivity(intent)
  }

  fun showRemovePatientReasonSheet(appointmentUuid: UUID, patientUuid: UUID) {
    val intent = RemoveAppointmentScreen.intent(context, appointmentUuid, patientUuid)
    activity.startActivity(intent)
  }

  fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    activity.startActivity(PhoneMaskBottomSheet.intentForPhoneMaskBottomSheet(context, patientUuid))
  }
}
