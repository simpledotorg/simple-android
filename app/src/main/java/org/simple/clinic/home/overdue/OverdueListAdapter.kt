package org.simple.clinic.home.overdue

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.locationRectOnScreen
import org.simple.clinic.widgets.marginLayoutParams
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID
import javax.inject.Inject

class OverdueListAdapter @Inject constructor() : RecyclerView.Adapter<OverdueListViewHolder>() {

  companion object {
    private const val OVERDUE_PATIENT = R.layout.item_overdue_list_patient
  }

  var items: List<OverdueListItem> = emptyList()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  val itemClicks: Subject<UiEvent> = PublishSubject.create()

  override fun getItemViewType(position: Int) =
      when (items[position]) {
        is OverdueListItem.Patient -> OVERDUE_PATIENT
      }

  override fun getItemCount(): Int = items.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueListViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return OverdueListViewHolder.Patient(layout, itemClicks)
  }

  override fun onBindViewHolder(holder: OverdueListViewHolder, position: Int) {
    if (holder is OverdueListViewHolder.Patient) {
      holder.appointment = items[position] as OverdueListItem.Patient
      holder.render()
    }
  }
}

sealed class OverdueListItem {

  data class Patient(
      val appointmentUuid: UUID,
      val patientUuid: UUID,
      val name: String,
      val gender: Gender,
      val age: Int,
      val phoneNumber: String? = null,
      val bpSystolic: Int,
      val bpDiastolic: Int,
      val bpDaysAgo: Int,
      val overdueDays: Int,
      val isAtHighRisk: Boolean
  ) : OverdueListItem() {
    var cardExpanded = false
  }
}

sealed class OverdueListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  class Patient(
      itemView: View,
      private val eventStream: Subject<UiEvent>
  ) : OverdueListViewHolder(itemView) {

    private val patientNameTextView by bindView<TextView>(R.id.overdue_patient_name_age)
    private val patientBPTextView by bindView<TextView>(R.id.overdue_patient_bp)
    private val overdueDaysTextView by bindView<TextView>(R.id.overdue_days)
    private val isAtHighRiskTextView by bindView<TextView>(R.id.overdue_high_risk_label)
    private val callButton by bindView<ImageButton>(R.id.overdue_patient_call)
    private val actionsContainer by bindView<LinearLayout>(R.id.overdue_actions_container)
    private val phoneNumberTextView by bindView<TextView>(R.id.overdue_patient_phone_number)
    private val agreedToVisitTextView by bindView<TextView>(R.id.overdue_agreed_to_visit)
    private val remindLaterTextView by bindView<TextView>(R.id.overdue_reminder_later)
    private val removeFromListTextView by bindView<TextView>(R.id.overdue_remove_from_list)

    lateinit var appointment: OverdueListItem.Patient

    init {
      itemView.apply {
        setOnClickListener {
          appointment.cardExpanded = appointment.cardExpanded.not()
          if (appointment.cardExpanded) {
            eventStream.onNext(AppointmentExpanded(appointment.patientUuid))
          }
          updateBottomLayoutVisibility()
          updatePhoneNumberViewVisibility()

          post {
            val itemLocation = locationRectOnScreen()
            val itemBottomWithMargin = itemLocation.bottom + marginLayoutParams.bottomMargin

            val recyclerView = parent as RecyclerView
            val rvLocation = recyclerView.locationRectOnScreen()
            val differenceInBottoms = itemBottomWithMargin - rvLocation.bottom

            if (differenceInBottoms > 0) {
              recyclerView.smoothScrollBy(0, differenceInBottoms)
            }
          }
        }
      }
      callButton.setOnClickListener {
        eventStream.onNext(CallPatientClicked(appointment.patientUuid))
      }
      agreedToVisitTextView.setOnClickListener {
        eventStream.onNext(AgreedToVisitClicked(appointment.appointmentUuid))
      }
      remindLaterTextView.setOnClickListener {
        eventStream.onNext(RemindToCallLaterClicked(appointment.appointmentUuid))
      }
      removeFromListTextView.setOnClickListener {
        eventStream.onNext(RemoveFromListClicked(appointment.appointmentUuid, appointment.patientUuid))
      }
    }

    private fun updateBottomLayoutVisibility() {
      actionsContainer.visibleOrGone(appointment.cardExpanded)
    }

    private fun updatePhoneNumberViewVisibility() {
      phoneNumberTextView.visibleOrGone(appointment.cardExpanded && appointment.phoneNumber != null)
    }

    fun render() {
      val context = itemView.context

      patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, appointment.name, appointment.age)
      patientNameTextView.setCompoundDrawableStart(appointment.gender.displayIconRes)

      patientBPTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_patient_bp,
          appointment.bpDaysAgo,
          appointment.bpSystolic,
          appointment.bpDiastolic,
          appointment.bpDaysAgo
      )

      callButton.visibility = if (appointment.phoneNumber == null) GONE else VISIBLE
      phoneNumberTextView.text = appointment.phoneNumber

      isAtHighRiskTextView.visibility = if (appointment.isAtHighRisk) VISIBLE else GONE

      overdueDaysTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_overdue_days,
          appointment.overdueDays,
          appointment.overdueDays
      )

      updateBottomLayoutVisibility()
      updatePhoneNumberViewVisibility()
    }
  }
}
