package org.simple.clinic.summary.nextappointment

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.card.MaterialCardView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.AppointmentSheetOpenedFrom
import org.simple.clinic.summary.PatientSummaryChildView
import org.simple.clinic.summary.PatientSummaryModelUpdateCallback
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.summary.nextappointment.ui.NextAppointment
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

private typealias NextAppointmentActionClicked = () -> Unit

class NextAppointmentCardView(
    context: Context,
    attrs: AttributeSet
) : MaterialCardView(context, attrs),
    NextAppointmentUi,
    NextAppointmentUiActions,
    PatientSummaryChildView {

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenKeyProvider: ScreenKeyProvider

  @Inject
  @Named("full_date")
  lateinit var fullDateFormatter: DateTimeFormatter

  @Inject
  lateinit var effectHandlerFactory: NextAppointmentEffectHandler.Factory

  private val screenKey by unsafeLazy {
    screenKeyProvider.keyFor<PatientSummaryScreenKey>(this)
  }

  private val currentDate by unsafeLazy {
    LocalDate.now(userClock)
  }

  private var modelUpdateCallback: PatientSummaryModelUpdateCallback? = null

  var nextAppointmentActionClicks: NextAppointmentActionClicked? = null

  private var appointmentFacilityName by mutableStateOf("")
  private var actionButtonText by mutableStateOf("")
  private var appointmentSate by mutableStateOf<NextAppointmentState>(NextAppointmentState.NoAppointment)

  private val hotEvents = PublishSubject.create<NextAppointmentEvent>()

  private val events: Observable<NextAppointmentEvent> by unsafeLazy {
    hotEvents
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  private val delegate: MobiusDelegate<NextAppointmentModel, NextAppointmentEvent, NextAppointmentEffect> by unsafeLazy {
    val uiRenderer = NextAppointmentUiRenderer(this)

    MobiusDelegate.forView(
        events = events,
        defaultModel = NextAppointmentModel.default(screenKey.patientUuid, currentDate),
        init = NextAppointmentInit(),
        update = NextAppointmentUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = { model ->
          modelUpdateCallback?.invoke(model)
          uiRenderer.render(model)
        }
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)

    addView(ComposeView(context).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

      setContent {
        SimpleTheme {
          NextAppointment(
              state = appointmentSate,
              dateTimeFormatter = fullDateFormatter,
              facilityName = appointmentFacilityName,
              actionButtonText = actionButtonText
          ) {
            nextAppointmentActionClicks?.invoke()
          }
        }
      }
    })
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

  override fun registerSummaryModelUpdateCallback(callback: PatientSummaryModelUpdateCallback?) {
    modelUpdateCallback = callback
  }

  override fun showNoAppointment() {
    appointmentSate = NextAppointmentState.NoAppointment
  }

  override fun showAppointmentDate(date: LocalDate) {
    appointmentSate = NextAppointmentState.Today(date)
  }

  override fun showAppointmentDateWithRemainingDays(date: LocalDate, daysRemaining: Int) {
    appointmentSate = NextAppointmentState.Scheduled(date, daysRemaining)
  }

  override fun showAppointmentDateWithOverdueDays(date: LocalDate, overdueDays: Int) {
    appointmentSate = NextAppointmentState.Overdue(date, overdueDays)
  }

  override fun showAddAppointmentButton() {
    actionButtonText = context.getString(R.string.next_appointment_view_add)
  }

  override fun showChangeAppointmentButton() {
    actionButtonText = context.getString(R.string.next_appointment_view_change)
  }

  override fun showAppointmentFacility(name: String) {
    appointmentFacilityName = context.getString(R.string.next_appointment_appointment_at_facility, name)
  }

  override fun hideAppointmentFacility() {
    appointmentFacilityName = ""
  }

  override fun openScheduleAppointmentSheet(patientUUID: UUID) {
    router.push(ScheduleAppointmentSheet.Key(
        patientId = patientUUID,
        sheetOpenedFrom = AppointmentSheetOpenedFrom.DONE_CLICK
    ))
  }

  fun refreshAppointmentDetails() {
    hotEvents.onNext(RefreshAppointment)
  }

  interface Injector {
    fun inject(target: NextAppointmentCardView)
  }
}
