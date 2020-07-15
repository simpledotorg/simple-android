package org.simple.clinic.home.overdue

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_overdue.view.*
import org.simple.clinic.bindUiToController
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.main.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.UserClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.visibleOrGone
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class OverdueScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), OverdueUi {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: OverdueScreenController

  @Inject
  lateinit var userClock: UserClock

  @field:[Inject Named("full_date")]
  lateinit var dateFormatter: DateTimeFormatter

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
  }

  private fun screenCreates() = Observable.just(OverdueScreenCreated())

  override fun updateList(overdueAppointments: List<OverdueAppointment>, isDiabetesManagementEnabled: Boolean) {
    overdueListAdapter.submitList(OverdueAppointmentRow.from(
        appointments = overdueAppointments,
        clock = userClock,
        dateFormatter = dateFormatter,
        isDiabetesManagementEnabled = isDiabetesManagementEnabled
    ))
  }

  override fun handleEmptyList(isEmpty: Boolean) {
    viewForEmptyList.visibleOrGone(isEmpty)
    overdueRecyclerView.visibleOrGone(isEmpty.not())
  }

  override fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    activity.startActivity(ContactPatientBottomSheet.intent(context, patientUuid))
  }
}
