package org.simple.clinic.recentpatient

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.recent_patients_screen.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.recentpatientsview.RecentPatientItemTTypeDiffCallback
import org.simple.clinic.recentpatientsview.RecentPatientItemType
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
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

  @Inject
  lateinit var effectHandlerFactory: AllRecentPatientsEffectHandler.Factory

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            adapterEvents()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = AllRecentPatientsUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = AllRecentPatientsModel.create(),
        update = AllRecentPatientsUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = AllRecentPatientsInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val recentAdapter = ItemAdapter(RecentPatientItemTTypeDiffCallback())

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    setupScreen()

    bindUiToController(
        ui = this,
        events = events,
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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

  override fun updateRecentPatients(allItemTypes: List<RecentPatientItemType>) {
    recentAdapter.submitList(allItemTypes)
  }

  interface Injector {
    fun inject(target: RecentPatientsScreen)
  }
}
