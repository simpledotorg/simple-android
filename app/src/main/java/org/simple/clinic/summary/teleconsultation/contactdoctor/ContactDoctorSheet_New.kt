package org.simple.clinic.summary.teleconsultation.contactdoctor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.Observable
import kotlinx.android.synthetic.main.sheet_contact_doctor_new.*
import org.simple.clinic.R
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.DividerItemDecorator
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.dp
import javax.inject.Inject

class ContactDoctorSheet_New : BottomSheetActivity(), ContactDoctorUi {

  companion object {
    fun intent(context: Context): Intent {
      return Intent(context, ContactDoctorSheet_New::class.java)
    }
  }

  @Inject
  lateinit var effectHandler: ContactDoctorEffectHandler

  private val itemAdapter = ItemAdapter(DoctorListItem.DiffCallback())

  private val delegate by unsafeLazy {
    val uiRenderer = ContactDoctorUiRenderer(this)

    MobiusDelegate.forActivity(
        events = Observable.never(),
        defaultModel = ContactDoctorModel.create(),
        init = ContactDoctorInit(),
        update = ContactDoctorUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_contact_doctor_new)
    delegate.onRestoreInstanceState(savedInstanceState)

    doctorsRecyclerView.adapter = itemAdapter
    doctorsRecyclerView.addItemDecoration(DividerItemDecorator(
        context = this,
        marginStart = 16.dp,
        marginEnd = 16.dp
    ))
  }

  override fun showMedicalOfficers(medicalOfficers: List<MedicalOfficer>) {
    itemAdapter.submitList(DoctorListItem.from(medicalOfficers))
  }
}
