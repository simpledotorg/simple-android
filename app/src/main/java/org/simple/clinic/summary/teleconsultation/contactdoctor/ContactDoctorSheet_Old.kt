package org.simple.clinic.summary.teleconsultation.contactdoctor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.sheet_contact_doctor_old.*
import org.simple.clinic.R
import org.simple.clinic.facility.Facility
import org.simple.clinic.summary.teleconsultation.api.TeleconsultPhoneNumber
import org.simple.clinic.summary.teleconsultation.contactdoctor.PhoneNumberListItem.Event.PhoneNumberClicked
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.dp

class ContactDoctorSheet_Old : BottomSheetActivity() {

  companion object {
    private const val EXTRA_FACILITY_NAME = "facility_name"
    private const val EXTRA_PHONE_NUMBERS = "phone_numbers"
    private const val EXTRA_PHONE_NUMBER = "phone_number"

    fun intent(
        context: Context,
        facility: Facility,
        teleconsultationPhoneNumbers: List<TeleconsultPhoneNumber>
    ): Intent {
      val phoneNumbersArrayList = arrayListOf<TeleconsultPhoneNumber>().apply {
        addAll(teleconsultationPhoneNumbers)
      }

      return Intent(context, ContactDoctorSheet_Old::class.java).apply {
        putExtra(EXTRA_FACILITY_NAME, facility.name)
        putParcelableArrayListExtra(EXTRA_PHONE_NUMBERS, phoneNumbersArrayList)
      }
    }

    fun readPhoneNumberExtra(intent: Intent): String {
      return intent.getStringExtra(EXTRA_PHONE_NUMBER)!!
    }
  }

  private val facilityName: String by unsafeLazy {
    intent.getStringExtra(EXTRA_FACILITY_NAME)!!
  }

  private val phoneNumbers: ArrayList<TeleconsultPhoneNumber> by unsafeLazy {
    intent.getParcelableArrayListExtra(EXTRA_PHONE_NUMBERS)!!
  }

  private val itemAdapter = ItemAdapter(PhoneNumberListItem.DiffCallback())
  private val itemEventsDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_contact_doctor_old)

    contactDoctorSheetSubtitleTextView.text = getString(R.string.contactdoctor_subtitle, facilityName)
    phoneNumbersRecyclerView.adapter = itemAdapter
    phoneNumbersRecyclerView.addItemDecoration(DividerItemDecorator(
        context = this,
        marginStart = 16.dp,
        marginEnd = 16.dp
    ))

    itemAdapter.submitList(PhoneNumberListItem.from(phoneNumbers))

    val adapterSubscription = itemAdapter
        .itemEvents
        .subscribe {
          if (it is PhoneNumberClicked) {
            val intent = Intent()
            intent.putExtra(EXTRA_PHONE_NUMBER, it.phoneNumber)
            setResult(Activity.RESULT_OK, intent)
            finish()
          }
        }

    itemEventsDisposable.add(adapterSubscription)
  }

  override fun onDestroy() {
    itemEventsDisposable.dispose()
    super.onDestroy()
  }
}
