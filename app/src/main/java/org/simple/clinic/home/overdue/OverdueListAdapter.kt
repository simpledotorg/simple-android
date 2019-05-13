package org.simple.clinic.home.overdue

import android.annotation.SuppressLint
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.locationRectOnScreen
import org.simple.clinic.widgets.marginLayoutParams
import org.simple.clinic.widgets.setCompoundDrawableStart
import java.util.UUID
import javax.inject.Inject

class OverdueListAdapter @Inject constructor() : ListAdapter<OverdueListItem, OverdueListViewHolder>(OverdueListDiffer()) {

  private lateinit var recyclerView: RecyclerView

  val itemClicks = PublishSubject.create<UiEvent>()!!

  companion object {
    const val OVERDUE_HEADER = R.layout.item_overdue_list_header
    const val OVERDUE_PATIENT = R.layout.item_overdue_list_patient
  }

  override fun onAttachedToRecyclerView(rv: RecyclerView) {
    super.onAttachedToRecyclerView(rv)
    recyclerView = rv
  }

  override fun getItemViewType(position: Int) =
      when (getItem(position)) {
        is OverdueListItem.Header -> OVERDUE_HEADER
        is OverdueListItem.Patient -> OVERDUE_PATIENT
      }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueListViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    return when (viewType) {
      OVERDUE_HEADER -> OverdueListViewHolder.Header(layout)
      else -> OverdueListViewHolder.Patient(layout, itemClicks)
    }
  }

  override fun onBindViewHolder(holder: OverdueListViewHolder, position: Int) {
    if (holder is OverdueListViewHolder.Patient) {
      holder.appointment = getItem(position) as OverdueListItem.Patient
      holder.render()
    }
  }
}

sealed class OverdueListItem {

  object Header : OverdueListItem()

  @Parcelize
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
  ) : OverdueListItem(), Parcelable
}

sealed class OverdueListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  class Header(itemView: View) : OverdueListViewHolder(itemView)

  class Patient(
      itemView: View,
      private val eventStream: PublishSubject<UiEvent>
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
          toggleBottomLayoutVisibility()
          togglePhoneNumberViewVisibility()

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
        eventStream.onNext(CallPatientClicked(appointment))
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

    private fun toggleBottomLayoutVisibility() {
      val isVisible = actionsContainer.visibility == VISIBLE
      actionsContainer.visibility =
          if (isVisible) {
            GONE
          } else {
            eventStream.onNext(AppointmentExpanded(appointment.patientUuid))
            VISIBLE
          }
    }

    private fun togglePhoneNumberViewVisibility() {
      val isVisible = phoneNumberTextView.visibility == VISIBLE
      if (!isVisible && appointment.phoneNumber != null) {
        phoneNumberTextView.visibility = VISIBLE
      } else {
        phoneNumberTextView.visibility = GONE
      }
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
    }
  }
}

class OverdueListDiffer : DiffUtil.ItemCallback<OverdueListItem>() {

  override fun areItemsTheSame(oldItem: OverdueListItem, newItem: OverdueListItem): Boolean =
      if (oldItem is OverdueListItem.Patient && newItem is OverdueListItem.Patient) {
        oldItem.appointmentUuid == newItem.appointmentUuid
      } else {
        oldItem == newItem
      }

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(oldItem: OverdueListItem, newItem: OverdueListItem): Boolean = oldItem == newItem
}
