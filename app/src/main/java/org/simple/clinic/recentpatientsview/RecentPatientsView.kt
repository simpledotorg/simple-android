package org.simple.clinic.recentpatientsview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.recentpatient.RecentPatientsScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

  @Inject
  lateinit var controller: RecentPatientsViewController

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val recyclerView by bindView<RecyclerView>(R.id.recentpatients_recyclerview)
  private val emptyStateView by bindView<View>(R.id.recentpatients_no_recent_patients)

  private val groupAdapter = GroupAdapter<ViewHolder>()
  private val adapterEvents = PublishSubject.create<UiEvent>()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    inflate(context, R.layout.recent_patients, this)

    recyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = groupAdapter
    }

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), adapterEvents),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  fun updateRecentPatients(recentPatients: List<RecentPatientItem>) {
    recentPatients.forEach { it.uiEvents = adapterEvents }
    groupAdapter.update(recentPatients)
  }

  fun showEmptyState() {
    emptyStateView.visibleOrGone(true)
    recyclerView.visibleOrGone(false)
  }

  fun hideEmptyState() {
    emptyStateView.visibleOrGone(false)
    recyclerView.visibleOrGone(true)
  }

  fun openRecentPatientsScreen() {
    screenRouter.push(RecentPatientsScreenKey())
  }

  fun openPatientSummary(patientUuid: UUID) {
    activity.screenRouter.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        ))
  }
}
