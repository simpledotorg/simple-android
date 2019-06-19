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

class OverdueListAdapter(
    private val onToggleCardExpansion: (Int) -> Unit
) : RecyclerView.Adapter<OverdueListViewHolder>() {

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
    return OverdueListViewHolder.Patient(layout, itemClicks, onToggleCardExpansion)
  }

  override fun onBindViewHolder(holder: OverdueListViewHolder, position: Int) {
    if (holder is OverdueListViewHolder.Patient) {
      holder.bind(items[position] as OverdueListItem.Patient)
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
      private val eventStream: Subject<UiEvent>,
      private val onToggleCardExpansion: (Int) -> Unit
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

    fun bind(overdueAppointmentListItem: OverdueListItem.Patient) {
      setupEvents(overdueAppointmentListItem)
      render(overdueAppointmentListItem)
    }

    private fun setupEvents(overdueAppointmentListItem: OverdueListItem.Patient) {
      itemView.setOnClickListener {
        overdueAppointmentListItem.cardExpanded = overdueAppointmentListItem.cardExpanded.not()
        if (overdueAppointmentListItem.cardExpanded) {
          eventStream.onNext(AppointmentExpanded(overdueAppointmentListItem.patientUuid))
        }
        updateBottomLayoutVisibility(overdueAppointmentListItem)
        updatePhoneNumberViewVisibility(overdueAppointmentListItem)

        itemView.post {
          val itemLocation = itemView.locationRectOnScreen()
          val itemBottomWithMargin = itemLocation.bottom + itemView.marginLayoutParams.bottomMargin
          onToggleCardExpansion(itemBottomWithMargin)
        }
      }
      callButton.setOnClickListener {
        eventStream.onNext(CallPatientClicked(overdueAppointmentListItem.patientUuid))
      }
      agreedToVisitTextView.setOnClickListener {
        eventStream.onNext(AgreedToVisitClicked(overdueAppointmentListItem.appointmentUuid))
      }
      remindLaterTextView.setOnClickListener {
        eventStream.onNext(RemindToCallLaterClicked(overdueAppointmentListItem.appointmentUuid))
      }
      removeFromListTextView.setOnClickListener {
        eventStream.onNext(RemoveFromListClicked(overdueAppointmentListItem.appointmentUuid, overdueAppointmentListItem.patientUuid))
      }
    }

    private fun render(overdueAppointmentListItem: OverdueListItem.Patient) {
      val context = itemView.context

      patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, overdueAppointmentListItem.name, overdueAppointmentListItem.age)
      patientNameTextView.setCompoundDrawableStart(overdueAppointmentListItem.gender.displayIconRes)

      patientBPTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_patient_bp,
          overdueAppointmentListItem.bpDaysAgo,
          overdueAppointmentListItem.bpSystolic,
          overdueAppointmentListItem.bpDiastolic,
          overdueAppointmentListItem.bpDaysAgo
      )

      callButton.visibility = if (overdueAppointmentListItem.phoneNumber == null) GONE else VISIBLE
      phoneNumberTextView.text = overdueAppointmentListItem.phoneNumber

      isAtHighRiskTextView.visibility = if (overdueAppointmentListItem.isAtHighRisk) VISIBLE else GONE

      overdueDaysTextView.text = context.resources.getQuantityString(
          R.plurals.overdue_list_item_overdue_days,
          overdueAppointmentListItem.overdueDays,
          overdueAppointmentListItem.overdueDays
      )

      updateBottomLayoutVisibility(overdueAppointmentListItem)
      updatePhoneNumberViewVisibility(overdueAppointmentListItem)
    }

    private fun updateBottomLayoutVisibility(overdueAppointmentListItem: OverdueListItem.Patient) {
      actionsContainer.visibleOrGone(overdueAppointmentListItem.cardExpanded)
    }

    private fun updatePhoneNumberViewVisibility(overdueAppointmentListItem: OverdueListItem.Patient) {
      val shouldShowPhoneNumberView = overdueAppointmentListItem.cardExpanded && overdueAppointmentListItem.phoneNumber != null
      phoneNumberTextView.visibleOrGone(shouldShowPhoneNumberView)
    }
  }
}
