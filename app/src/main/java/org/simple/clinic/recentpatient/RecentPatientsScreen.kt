package org.simple.clinic.recentpatient

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.recent_patients_screen.view.*
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RecentPatientsScreen(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs), AllRecentPatientsUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: RecentPatientsScreenController

  @Inject
  lateinit var utcClock: UtcClock

  private val recentAdapter = ItemAdapter(RecentPatientItem.DiffCallback())

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    setupScreen()

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), adapterEvents()),
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun setupScreen() {
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }

    recyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = recentAdapter
    }
  }

  private fun adapterEvents(): Observable<UiEvent> {
    return recentAdapter
        .itemEvents
        .ofType()
  }

  override fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        ))
  }

  override fun updateRecentPatients(allItemTypes: List<RecentPatientItem>) {
    recentAdapter.submitList(allItemTypes)
  }

  interface Injector {
    fun inject(target: RecentPatientsScreen)
  }
}
