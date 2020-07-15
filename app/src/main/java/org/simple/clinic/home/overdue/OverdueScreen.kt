package org.simple.clinic.home.overdue

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_overdue.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.di.injector
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
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

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            overdueListAdapter.itemEvents
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)

    val screenDestroys = detaches().map { ScreenDestroyed() }

    bindUiToController(
        ui = this,
        events = events,
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

  interface Injector {
    fun inject(target: OverdueScreen)
  }
}
