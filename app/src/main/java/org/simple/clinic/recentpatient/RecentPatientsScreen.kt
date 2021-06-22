package org.simple.clinic.recentpatient

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.RecentPatientItemViewBinding
import org.simple.clinic.databinding.ScreenRecentPatientsBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class RecentPatientsScreen : BaseScreen<
    RecentPatientsScreen.Key,
    ScreenRecentPatientsBinding,
    AllRecentPatientsModel,
    AllRecentPatientsEvent,
    AllRecentPatientsEffect>(), AllRecentPatientsUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var effectHandlerFactory: AllRecentPatientsEffectHandler.Factory

  @Named("full_date")
  @Inject
  lateinit var fullDateFormatter: DateTimeFormatter

  private val toolbar
    get() = binding.toolbar

  private val recyclerView
    get() = binding.recyclerView

  private val recentAdapter = PagingItemAdapter(
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

  override fun defaultModel() = AllRecentPatientsModel

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenRecentPatientsBinding.inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupScreen()
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

  override fun showRecentPatients(recentPatients: PagingData<RecentPatient>) {
    recentAdapter.submitData(lifecycle, RecentPatientItem.create(recentPatients, userClock, fullDateFormatter))
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
