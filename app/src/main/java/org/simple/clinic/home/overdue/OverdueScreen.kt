package org.simple.clinic.home.overdue

import android.Manifest
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.home.overdue.OverdueListItem.Patient
import org.simple.clinic.home.overdue.appointmentreminder.AppointmentReminderSheet
import org.simple.clinic.home.overdue.phonemask.PhoneMaskBottomSheet
import org.simple.clinic.home.overdue.removepatient.RemoveAppointmentScreen
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.ScreenDestroyed
import java.util.UUID
import javax.inject.Inject

private const val REQUESTCODE_CALL_PHONE_PERMISSION = 17
private const val CALL_PHONE_PERMISSION = Manifest.permission.CALL_PHONE

class OverdueScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: OverdueScreenController

  @Inject
  lateinit var overdueListAdapter: OverdueListAdapter

  private val overdueRecyclerView by bindView<RecyclerView>(R.id.overdue_list)
  private val viewForEmptyList by bindView<LinearLayout>(R.id.overdue_list_empty_layout)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            callPermissionChanges(),
            overdueListAdapter.itemClicks
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just(OverdueScreenCreated())

  private fun callPermissionChanges(): Observable<CallPhonePermissionChanged> {
    return screenRouter.streamScreenResults()
        .ofType<ActivityPermissionResult>()
        .filter { result -> result.requestCode == REQUESTCODE_CALL_PHONE_PERMISSION }
        .map { RuntimePermissions.check(activity, CALL_PHONE_PERMISSION) }
        .map(::CallPhonePermissionChanged)
  }

  fun updateList(overdueListItems: List<OverdueListItem>) {
    overdueListAdapter.submitList(overdueListItems)
  }

  fun handleEmptyList(isEmpty: Boolean) {
    TransitionManager.beginDelayedTransition(this)
    if (isEmpty) {
      overdueRecyclerView.visibility = View.GONE
      viewForEmptyList.visibility = View.VISIBLE
    } else {
      overdueRecyclerView.visibility = View.VISIBLE
      viewForEmptyList.visibility = View.GONE
    }
  }

  fun requestCallPermission() {
    RuntimePermissions.request(activity, CALL_PHONE_PERMISSION, REQUESTCODE_CALL_PHONE_PERMISSION)
  }

  fun showAppointmentReminderSheet(appointmentUuid: UUID) {
    val intent = AppointmentReminderSheet.intent(context, appointmentUuid)
    activity.startActivity(intent)
  }

  fun showRemovePatientReasonSheet(appointmentUuid: UUID, patientUuid: UUID) {
    val intent = RemoveAppointmentScreen.intent(context, appointmentUuid, patientUuid)
    activity.startActivity(intent)
  }

  fun openPhoneMaskBottomSheet(patient: Patient) {
    activity.startActivity(PhoneMaskBottomSheet.intentForPhoneMaskBottomSheet(context, patient))
  }
}
