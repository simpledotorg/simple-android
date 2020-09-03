package org.simple.clinic.home.overdue

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_overdue.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.PagingItemAdapter
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class OverdueScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), OverdueUiActions {

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var screenRouter: ScreenRouter

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

  private val overdueListAdapter = PagingItemAdapter(OverdueAppointmentRow.DiffCallback())

  private val events by unsafeLazy {
    overdueListAdapter
        .itemEvents
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val date = LocalDate.now(userClock)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = OverdueModel.create(),
        update = OverdueUpdate(date),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = OverdueInit(),
        modelUpdateListener = { /* Nothing to do here */ }
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    overdueRecyclerView.adapter = overdueListAdapter
    overdueRecyclerView.layoutManager = LinearLayoutManager(context)
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

  override fun openPhoneMaskBottomSheet(patientUuid: UUID) {
    activity.startActivity(ContactPatientBottomSheet.intent(context, patientUuid))
  }

  @SuppressLint("CheckResult")
  override fun showOverdueAppointments(dataSource: OverdueAppointmentRowDataSource.Factory) {
    val detaches = detaches()

    dataSource
        .toObservable(pagedListConfig, detaches)
        .takeUntil(detaches)
        .doOnNext { appointmentsList ->
          val areOverdueAppointmentsAvailable = appointmentsList.isNotEmpty()

          viewForEmptyList.visibleOrGone(isVisible = !areOverdueAppointmentsAvailable)
          overdueRecyclerView.visibleOrGone(isVisible = areOverdueAppointmentsAvailable)
        }
        .subscribe(overdueListAdapter::submitList)
  }

  override fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  interface Injector {
    fun inject(target: OverdueScreen)
  }
}
