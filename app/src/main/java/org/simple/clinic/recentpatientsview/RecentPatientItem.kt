package org.simple.clinic.recentpatientsview

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.patient.Gender
import org.simple.clinic.recentpatientsview.SeeAllItem.SeeAllItemViewHolder
import org.simple.clinic.summary.GroupieItemWithUiEvents
import org.simple.clinic.summary.RelativeTimestamp
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class RecentPatientItemType<VH : ViewHolder>(adapterId: Long) : GroupieItemWithUiEvents<VH>(adapterId) {
  override lateinit var uiEvents: Subject<UiEvent>
}

data class RecentPatientItem(
    val uuid: UUID,
    val name: String,
    val age: Int,
    val gender: Gender,
    val updatedAt: RelativeTimestamp
) : RecentPatientItemType<RecentPatientItem.RecentPatientViewHolder>(uuid.hashCode().toLong()) {

  override fun getLayout(): Int = R.layout.recent_patient_item_view

  override fun createViewHolder(itemView: View): RecentPatientViewHolder {
    return RecentPatientViewHolder(itemView)
  }

  @SuppressLint("SetTextI18n")
  override fun bind(viewHolder: RecentPatientViewHolder, position: Int) {
    viewHolder.itemView.setOnClickListener {
      uiEvents.onNext(RecentPatientItemClicked(patientUuid = uuid))
    }

    viewHolder.render(name, age, gender, updatedAt)
  }

  class RecentPatientViewHolder(rootView: View) : ViewHolder(rootView) {
    private val nameAgeTextView by bindView<TextView>(R.id.recentpatient_item_title)
    private val lastSeenTextView by bindView<TextView>(R.id.recentpatient_item_last_seen)
    private val genderImageView by bindView<ImageView>(R.id.recentpatient_item_gender)

    @SuppressLint("SetTextI18n")
    fun render(
        name: String,
        age: Int,
        gender: Gender,
        updatedAt: RelativeTimestamp
    ) {
      nameAgeTextView.text = itemView.resources.getString(R.string.patients_recentpatients_nameage, name, age)
      genderImageView.setImageResource(gender.displayIconRes)
      lastSeenTextView.text = updatedAt.displayText(itemView.context)
    }
  }
}

object SeeAllItem : RecentPatientItemType<SeeAllItemViewHolder>(0) {
  override fun getLayout() = R.layout.see_all_item_view

  override fun createViewHolder(itemView: View): SeeAllItemViewHolder {
    return SeeAllItemViewHolder(itemView)
  }

  override fun bind(viewHolder: SeeAllItemViewHolder, position: Int) {
    viewHolder.seeAllButton.setOnClickListener {
      uiEvents.onNext(SeeAllItemClicked)
    }
  }

  class SeeAllItemViewHolder(rootView: View) : ViewHolder(rootView) {
    val seeAllButton by bindView<View>(R.id.seeall_button)
  }
}
