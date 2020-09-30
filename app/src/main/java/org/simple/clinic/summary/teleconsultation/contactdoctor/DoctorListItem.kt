package org.simple.clinic.summary.teleconsultation.contactdoctor

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_contact_doctor.*
import org.simple.clinic.R
import org.simple.clinic.summary.teleconsultation.contactdoctor.DoctorListItem.Event.SmsClicked
import org.simple.clinic.summary.teleconsultation.contactdoctor.DoctorListItem.Event.WhatsAppClicked
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX
import java.util.UUID

data class DoctorListItem(
    private val doctorId: UUID,
    private val name: String,
    private val number: String
) : ItemAdapter.Item<DoctorListItem.Event> {

  companion object {
    fun from(medicalOfficers: List<MedicalOfficer>): List<DoctorListItem> {
      return medicalOfficers
          .map {
            DoctorListItem(
                doctorId = it.medicalOfficerId,
                name = it.fullName,
                number = it.phoneNumber
            )
          }
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<DoctorListItem>() {
    override fun areItemsTheSame(oldItem: DoctorListItem, newItem: DoctorListItem): Boolean {
      return oldItem.doctorId == newItem.doctorId
    }

    override fun areContentsTheSame(oldItem: DoctorListItem, newItem: DoctorListItem): Boolean {
      return oldItem == newItem
    }
  }

  override fun layoutResId(): Int = R.layout.list_contact_doctor

  override fun render(holder: ViewHolderX, subject: Subject<Event>) {
    holder.doctorNameTextView.text = name
    holder.doctorNumberTextView.text = number

    holder.whatsAppDoctorImageView.setOnClickListener {
      subject.onNext(WhatsAppClicked(
          doctorId = doctorId,
          phoneNumber = number
      ))
    }
    holder.messageDoctorImageView.setOnClickListener {
      subject.onNext(SmsClicked(
          doctorId = doctorId,
          phoneNumber = number
      ))
    }
  }

  sealed class Event {

    data class WhatsAppClicked(val doctorId: UUID, val phoneNumber: String) : Event()

    data class SmsClicked(val doctorId: UUID, val phoneNumber: String) : Event()
  }
}
