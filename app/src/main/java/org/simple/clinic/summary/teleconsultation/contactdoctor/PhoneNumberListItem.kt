package org.simple.clinic.summary.teleconsultation.contactdoctor

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_contact_doctor_phone_number.*
import org.simple.clinic.R
import org.simple.clinic.summary.teleconsultation.api.TeleconsultPhoneNumber
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX

data class PhoneNumberListItem(
    private val indexLabel: String,
    private val number: String
) : ItemAdapter.Item<PhoneNumberListItem.Event> {

  companion object {
    private fun getAlphabetIndex(index: Int): Char {
      return if (index < 0 || index > 25) ' ' else ('A' + index)
    }

    fun from(numbers: List<TeleconsultPhoneNumber>): List<PhoneNumberListItem> {
      return numbers
          .mapIndexed { index, teleconsultPhoneNumber ->
            val indexLabel = getAlphabetIndex(index).toString()
            val phoneNumber = teleconsultPhoneNumber.phoneNumber

            PhoneNumberListItem(indexLabel = indexLabel, number = phoneNumber)
          }
    }
  }

  class DiffCallback : DiffUtil.ItemCallback<PhoneNumberListItem>() {
    override fun areItemsTheSame(oldItem: PhoneNumberListItem, newItem: PhoneNumberListItem): Boolean {
      return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: PhoneNumberListItem, newItem: PhoneNumberListItem): Boolean {
      return oldItem == newItem
    }
  }

  override fun layoutResId(): Int = R.layout.list_contact_doctor_phone_number

  @SuppressLint("SetTextI18n")
  override fun render(holder: ViewHolderX, subject: Subject<Event>) {
    holder.phoneNumberIndexTextView.text = "$indexLabel."
    holder.phoneNumberTextView.text = number
    holder.itemView.setOnClickListener { subject.onNext(Event.PhoneNumberClicked(number)) }
  }

  sealed class Event {
    data class PhoneNumberClicked(val phoneNumber: String) : Event()
  }
}
