package org.simple.clinic.home.overdue

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.spotify.mobius.Update
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.databinding.ItemOverdueListPatientBinding
import org.simple.clinic.databinding.ScreenOverdueBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class OverdueScreen : BaseScreen<
    OverdueScreen.Key,
    ScreenOverdueBinding,
    OverdueModel,
    OverdueEvent,
    OverdueEffect>(), OverdueUiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: OverdueEffectHandler.Factory

  @Inject
  @Named("for_overdue_appointments")
  lateinit var pagedListConfig: PagedList.Config

  private val overdueListAdapter = PagingItemAdapter(
      diffCallback = OverdueAppointmentRow.DiffCallback(),
      bindings = mapOf(
          R.layout.item_overdue_list_patient to { layoutInflater, parent ->
            ItemOverdueListPatientBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val viewForEmptyList
    get() = binding.viewForEmptyList

  private val overdueRecyclerView
    get() = binding.overdueRecyclerView

  private val screenDestroys = PublishSubject.create<Unit>()

  override fun defaultModel() = OverdueModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenOverdueBinding.inflate(layoutInflater, container, false)

  override fun events() = overdueListAdapter
      .itemEvents
      .compose(ReportAnalyticsEvents())
      .share()
      .cast<OverdueEvent>()

  override fun createUpdate(): Update<OverdueModel, OverdueEvent, OverdueEffect> {
    val date = LocalDate.now(userClock)
    return OverdueUpdate(date)
  }

  override fun createInit() = OverdueInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(Unit)
  }

  override fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    router.push(ContactPatientBottomSheet.Key(patientUuid))
  }

  @SuppressLint("CheckResult")
  override fun showOverdueAppointments(dataSource: OverdueAppointmentRowDataSource.Factory) {
    dataSource
        .toObservable(pagedListConfig, screenDestroys)
        .takeUntil(screenDestroys)
        .doOnNext { appointmentsList ->
          val areOverdueAppointmentsAvailable = appointmentsList.isNotEmpty()

          viewForEmptyList.visibleOrGone(isVisible = !areOverdueAppointmentsAvailable)
          overdueRecyclerView.visibleOrGone(isVisible = areOverdueAppointmentsAvailable)
        }
        .subscribe(overdueListAdapter::submitList)
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

  interface Injector {
    fun inject(target: OverdueScreen)
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Overdue"

    override fun instantiateFragment() = OverdueScreen()
  }
}
