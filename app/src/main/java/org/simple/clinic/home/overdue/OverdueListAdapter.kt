package org.simple.clinic.home.overdue

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.widgets.locationRectOnScreen
import org.simple.clinic.widgets.marginLayoutParams
import java.util.UUID

class OverdueListAdapter(
    private val phoneCallClickStream: PublishSubject<CallPatientClicked>
) : ListAdapter<OverdueListItem, OverdueListViewHolder>(OverdueListDiffer()) {

  private lateinit var recyclerView: RecyclerView

  override fun onAttachedToRecyclerView(rv: RecyclerView) {
    super.onAttachedToRecyclerView(rv)
    recyclerView = rv
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverdueListViewHolder {
    val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_overdue_list, parent, false)
    val holder = OverdueListViewHolder(layout, phoneCallClickStream)

    layout.setOnClickListener {
      holder.handleBottomLayoutVisibility()

      holder.itemView.post {
        val itemLocation = holder.itemView.locationRectOnScreen()
        val itemBottomWithMargin = itemLocation.bottom + holder.itemView.marginLayoutParams.bottomMargin

        val rvLocation = recyclerView.locationRectOnScreen()
        val differenceInBottoms = itemBottomWithMargin - rvLocation.bottom

        if (differenceInBottoms > 0) {
          (holder.itemView.parent as RecyclerView).smoothScrollBy(0, differenceInBottoms)
        }
      }
    }

    return holder
  }

  override fun onBindViewHolder(holder: OverdueListViewHolder, position: Int) {
    holder.appointment = getItem(position)
    holder.render()
  }
}

data class OverdueListItem(
    val appointmentUuid: UUID,
    val name: String,
    val gender: Gender,
    val age: Int,
    val phoneNumber: String? = null,
    val bpSystolic: Int,
    val bpDiastolic: Int,
    val bpDaysAgo: Int,
    val overdueDays: Int
)

class OverdueListViewHolder(
    itemView: View,
    private val phoneCallClickStream: PublishSubject<CallPatientClicked>
) : RecyclerView.ViewHolder(itemView) {

  private val patientNameTextView by bindView<TextView>(R.id.overdue_patient_name_age)
  private val patientBPTextView by bindView<TextView>(R.id.overdue_patient_bp)
  private val overdueDaysTextView by bindView<TextView>(R.id.overdue_days)
  private val callButton by bindView<ImageButton>(R.id.overdue_patient_call)
  private val actionsContainer by bindView<LinearLayout>(R.id.overdue_actions_container)
  private val phoneNumberTextView by bindView<TextView>(R.id.overdue_patient_phone_number)

  lateinit var appointment: OverdueListItem

  init {
    callButton.setOnClickListener {
      phoneCallClickStream.onNext(CallPatientClicked(appointment.phoneNumber!!))
    }
  }

  fun handleBottomLayoutVisibility() {
    val isVisible = actionsContainer.visibility == View.VISIBLE
    actionsContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
  }

  fun render() {
    val context = itemView.context

    patientNameTextView.text = context.getString(R.string.overdue_list_item_name_age, appointment.name, appointment.age)

    patientBPTextView.text = context.resources.getQuantityString(
        R.plurals.overdue_list_item_patient_bp,
        appointment.bpDaysAgo,
        appointment.bpSystolic,
        appointment.bpDiastolic,
        appointment.bpDaysAgo
    )

    if (appointment.phoneNumber == null) {
      callButton.visibility = View.GONE
      phoneNumberTextView.visibility = View.GONE
    } else {
      callButton.visibility = View.VISIBLE
      phoneNumberTextView.visibility = View.VISIBLE
    }

    phoneNumberTextView.text = appointment.phoneNumber

    overdueDaysTextView.text = context.resources.getQuantityString(
        R.plurals.overdue_list_item_overdue_days,
        appointment.overdueDays,
        appointment.overdueDays
    )
  }
}

class OverdueListDiffer : DiffUtil.ItemCallback<OverdueListItem>() {

  override fun areItemsTheSame(oldItem: OverdueListItem, newItem: OverdueListItem): Boolean = oldItem.appointmentUuid == newItem.appointmentUuid

  override fun areContentsTheSame(oldItem: OverdueListItem, newItem: OverdueListItem): Boolean = oldItem == newItem
}
