package org.simple.clinic.recentpatientsview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.recent_patients.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.recentpatient.RecentPatientsScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RecentPatientsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), LatestRecentPatientsUi {

  @Inject
  lateinit var controller: RecentPatientsViewController

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val recentAdapter = ItemAdapter(RecentPatientItemTTypeDiffCallback())
  private val detaches = detaches()

  override fun onFinishInflate() {
    super.onFinishInflate()
    TheActivity.component.inject(this)

    inflate(context, R.layout.recent_patients, this)

    recentRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = recentAdapter
      isNestedScrollingEnabled = false
    }

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), adapterEvents()),
        controller = controller,
        screenDestroys = detaches.map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun adapterEvents() = recentAdapter.itemEvents.ofType<UiEvent>()

  override fun updateRecentPatients(recentPatients: List<RecentPatientItemType>) {
    recentAdapter.submitList(recentPatients)
  }

  override fun showOrHideRecentPatients(isVisible: Boolean) {
    recentRecyclerView.visibleOrGone(isVisible)
    noRecentPatientsTextView.visibleOrGone(isVisible.not())
  }

  override fun openRecentPatientsScreen() {
    screenRouter.push(RecentPatientsScreenKey())
  }

  override fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        ))
  }
}
