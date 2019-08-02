package org.simple.clinic.home.overdue

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_overdue_list_patient.*
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.displayIconRes
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.locationRectOnScreen
import org.simple.clinic.widgets.marginLayoutParams
import org.simple.clinic.widgets.setCompoundDrawableStart
import org.simple.clinic.widgets.visibleOrGone
import java.util.UUID

class OverdueListAdapter : RecyclerView.Adapter<OverdueListViewHolder>() {

  companion object {
    private const val OVERDUE_PATIENT = R.layout.item_overdue_list_patient
  }

  var items: List<OverdueListItem> = emptyList()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  private val eventsSubject: Subject<UiEvent> = PublishSubject.create()

  val uiEvents: Observable<UiEvent> = eventsSubject.hide()

  override fun getItemViewType(position: Int) =
      when (items[position]) {
        is OverdueListItem.Patient -> OVERDUE_PATIENT
      }

  override fun getItemCount(): Int = items.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueListViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return OverdueListViewHolder.Patient(layout)
  }

  override fun onBindViewHolder(holder: OverdueListViewHolder, position: Int) {
    if (holder is OverdueListViewHolder.Patient) {
      holder.bind(items[position] as OverdueListItem.Patient, eventsSubject)
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

sealed class OverdueListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

  class Patient(override val containerView: View) : OverdueListViewHolder(containerView) {

    fun bind(
        overdueAppointmentListItem: OverdueListItem.Patient,
        eventsSubject: Subject<UiEvent>
    ) {
      setupEvents(overdueAppointmentListItem, eventsSubject)
      render(overdueAppointmentListItem)
    }

    private fun setupEvents(
        overdueAppointmentListItem: OverdueListItem.Patient,
        eventSubject: Subject<UiEvent>
    ) {
      containerView.setOnClickListener {
        overdueAppointmentListItem.cardExpanded = overdueAppointmentListItem.cardExpanded.not()
        if (overdueAppointmentListItem.cardExpanded) {
          eventSubject.onNext(AppointmentExpanded(overdueAppointmentListItem.patientUuid))
        }
        updateBottomLayoutVisibility(overdueAppointmentListItem)
        updatePhoneNumberViewVisibility(overdueAppointmentListItem)

        containerView.post {
          val itemLocation = containerView.locationRectOnScreen()
          val itemBottomWithMargin = itemLocation.bottom + containerView.marginLayoutParams.bottomMargin
          eventSubject.onNext(CardExpansionToggled(itemBottomWithMargin))
        }
      }
      callButton.setOnClickListener {
        eventSubject.onNext(CallPatientClicked(overdueAppointmentListItem.patientUuid))
      }
      agreedToVisitTextView.setOnClickListener {
        eventSubject.onNext(AgreedToVisitClicked(overdueAppointmentListItem.appointmentUuid))
      }
      remindLaterTextView.setOnClickListener {
        eventSubject.onNext(RemindToCallLaterClicked(overdueAppointmentListItem.appointmentUuid))
      }
      removeFromListTextView.setOnClickListener {
        eventSubject.onNext(RemoveFromListClicked(overdueAppointmentListItem.appointmentUuid, overdueAppointmentListItem.patientUuid))
      }
    }

    private fun render(overdueAppointmentListItem: OverdueListItem.Patient) {
      val context = containerView.context

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

    data class CardExpansionToggled(val cardBottomWithMargin: Int) : UiEvent
  }
}
