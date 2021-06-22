package org.simple.clinic.recentpatient

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.RecentPatientItemViewBinding
import org.simple.clinic.databinding.RecentPatientsScreenBinding
import org.simple.clinic.databinding.ScreenRecentPatientsBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class RecentPatientsScreen : BaseScreen<
    RecentPatientsScreen.Key,
    ScreenRecentPatientsBinding,
    AllRecentPatientsModel,
    AllRecentPatientsEvent,
    AllRecentPatientsEffect>(), AllRecentPatientsUi, AllRecentPatientsUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var effectHandlerFactory: AllRecentPatientsEffectHandler.Factory

  @Inject
  lateinit var uiRendererFactory: AllRecentPatientsUiRenderer.Factory

  private val toolbar
    get() = binding.toolbar

  private val recyclerView
    get() = binding.recyclerView

  private val recentAdapter = ItemAdapter(
      diffCallback = RecentPatientItemDiffCallback(),
      bindings = mapOf(
          R.layout.recent_patient_item_view to { layoutInflater, parent ->
            RecentPatientItemViewBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun events(): Observable<AllRecentPatientsEvent> {
    return adapterEvents()
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  override fun createInit() = AllRecentPatientsInit()

  override fun createUpdate() = AllRecentPatientsUpdate()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun defaultModel() = AllRecentPatientsModel.create()

  override fun uiRenderer() = uiRendererFactory.create(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRecentPatientsBinding.inflate(layoutInflater, container, false)

  private val delegate by unsafeLazy {
    val uiRenderer = uiRendererFactory.create(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = AllRecentPatientsModel.create(),
        update = AllRecentPatientsUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = AllRecentPatientsInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = RecentPatientsScreenBinding.bind(this)

    context.injector<Injector>().inject(this)

    setupScreen()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun setupScreen() {
    toolbar.setNavigationOnClickListener {
      router.pop()
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
    router.push(
        PatientSummaryScreenKey(
            patientUuid = patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        )
    )
  }

  override fun updateRecentPatients(allItemTypes: List<RecentPatientItem>) {
    recentAdapter.submitList(allItemTypes)
  }

  interface Injector {
    fun inject(target: RecentPatientsScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Recent Patient Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = RecentPatientsScreen()
  }
}
