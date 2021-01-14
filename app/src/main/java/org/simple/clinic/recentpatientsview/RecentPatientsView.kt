package org.simple.clinic.recentpatientsview

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.recent_patients.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.RecentPatientItemViewBinding
import org.simple.clinic.databinding.SeeAllItemViewBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.recentpatient.RecentPatientsScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RecentPatientsView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs), LatestRecentPatientsUi, LatestRecentPatientsUiActions {

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: LatestRecentPatientsEffectHandler.Factory

  @Inject
  lateinit var uiRendererFactory: LatestRecentPatientsUiRenderer.Factory

  @Inject
  lateinit var config: PatientConfig

  private val recentAdapter = ItemAdapter(
      diffCallback = RecentPatientItemTypeDiffCallback(),
      bindings = mapOf(
          R.layout.recent_patient_item_view to { layoutInflater, parent ->
            RecentPatientItemViewBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.see_all_item_view to { layoutInflater, parent ->
            SeeAllItemViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val events by unsafeLazy {
    adapterEvents()
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = uiRendererFactory.create(this, config.recentPatientLimit)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = LatestRecentPatientsModel.create(),
        update = LatestRecentPatientsUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = LatestRecentPatientsInit.create(config),
        modelUpdateListener = uiRenderer::render
    )
  }

  init {
    inflate(context, R.layout.recent_patients, this)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    recentRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = recentAdapter
      isNestedScrollingEnabled = false
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun adapterEvents() = recentAdapter.itemEvents.ofType<UiEvent>()

  override fun updateRecentPatients(recentPatients: List<RecentPatientItemType>) {
    recentAdapter.submitList(recentPatients)
  }

  override fun showOrHideRecentPatients(isVisible: Boolean) {
    recentRecyclerView.visibleOrGone(isVisible)
    noRecentPatientsTextView.visibleOrGone(isVisible.not())
  }

  override fun openRecentPatientsScreen() {
    router.push(RecentPatientsScreenKey().wrap())
  }

  override fun openPatientSummary(patientUuid: UUID) {
    router.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        ).wrap()
    )
  }

  interface Injector {
    fun inject(target: RecentPatientsView)
  }
}
