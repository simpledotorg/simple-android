package org.simple.clinic.summary.nextappointment

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.google.android.material.card.MaterialCardView
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.PatientsummaryNextAppointmentCardBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.summary.AppointmentSheetOpenedFrom
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.Unicode
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.resolveColor
import org.simple.clinic.util.unsafeLazy
import org.threeten.extra.Days
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.absoluteValue

class NextAppointmentCardView(
    context: Context,
    attrs: AttributeSet
) : MaterialCardView(context, attrs),
    NextAppointmentUi,
    NextAppointmentUiActions {

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

  private var binding: PatientsummaryNextAppointmentCardBinding? = null

  private val appointmentDateTextView
    get() = binding!!.appointmentDateTextView

  private val appointmentFacilityTextView
    get() = binding!!.appointmentFacilityTextView

  private val nextAppointmentActionsButton
    get() = binding!!.nextAppointmentActionButton

  private val events: Observable<NextAppointmentEvent> by unsafeLazy {
    actionsButtonClicks()
        .compose(ReportAnalyticsEvents())
        .cast()
  }

  private val delegate: MobiusDelegate<NextAppointmentModel, NextAppointmentEvent, NextAppointmentEffect> by unsafeLazy {
    val uiRenderer = NextAppointmentUiRenderer(this)

    MobiusDelegate.forView(
        events = events,
        defaultModel = NextAppointmentModel.default(screenKey.patientUuid),
        init = NextAppointmentInit(),
        update = NextAppointmentUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = { model ->
          uiRenderer.render(model)
        }
    )
  }

  init {
    val layoutInflater = LayoutInflater.from(context)
    binding = PatientsummaryNextAppointmentCardBinding.inflate(layoutInflater, this, true)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    context.injector<Injector>().inject(this)
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

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun showNoAppointment() {
    appointmentDateTextView.text = buildSpannedString {
      color(context.resolveColor(colorRes = R.color.color_on_surface_67)) {
        append(context.getString(R.string.next_appointment_none))
      }
    }
  }

  override fun showAppointmentDate(date: LocalDate) {
    val daysUntilAppointment = Days.between(currentDate, date)

    val formattedDate = fullDateFormatter.format(date)
    val appointmentStatus = appointmentStatusString(daysUntilAppointment)
    val appointmentStatusColor = appointmentStatusColor(daysUntilAppointment)

    appointmentDateTextView.text = buildSpannedString {
      color(context.resolveColor(attrRes = R.attr.colorOnSurface)) {
        append(formattedDate)
      }

      append(Unicode.nonBreakingSpace)

      color(appointmentStatusColor) {
        append(appointmentStatus)
      }
    }
  }

  private fun appointmentStatusColor(daysUntilAppointment: Days) = if (daysUntilAppointment >= Days.ZERO) {
    context.resolveColor(colorRes = R.color.simple_green_500)
  } else {
    context.resolveColor(attrRes = R.attr.colorError)
  }

  private fun appointmentStatusString(daysUntilAppointment: Days) = when {
    daysUntilAppointment < Days.ZERO -> {
      resources.getQuantityString(
          R.plurals.next_appointment_overdue_plurals,
          daysUntilAppointment.amount.absoluteValue,
          daysUntilAppointment.amount.absoluteValue
      )
    }
    daysUntilAppointment == Days.ZERO -> {
      resources.getString(R.string.next_appointment_today)
    }
    else -> {
      resources.getQuantityString(
          R.plurals.next_appointment_plurals,
          daysUntilAppointment.amount,
          daysUntilAppointment.amount
      )
    }
  }

  override fun showAddAppointmentButton() {
    nextAppointmentActionsButton.text = context.getString(R.string.next_appointment_view_add)
  }

  override fun showChangeAppointmentButton() {
    nextAppointmentActionsButton.text = context.getString(R.string.next_appointment_view_change)
  }

  override fun showAppointmentFacility(name: String) {
    appointmentFacilityTextView.visibility = View.VISIBLE
    appointmentFacilityTextView.text = context.getString(R.string.next_appointment_appointment_at_facility, name)
  }

  override fun hideAppointmentFacility() {
    appointmentFacilityTextView.visibility = View.GONE
  }

  override fun openScheduleAppointmentSheet(patientUUID: UUID) {
    router.push(ScheduleAppointmentSheet.Key(
        patientId = patientUUID,
        sheetOpenedFrom = AppointmentSheetOpenedFrom.DONE_CLICK
    ))
  }

  private fun actionsButtonClicks(): Observable<NextAppointmentEvent> {
    return nextAppointmentActionsButton
        .clicks()
        .map { NextAppointmentActionButtonClicked() }
  }

  interface Injector {
    fun inject(target: NextAppointmentCardView)
  }
}
