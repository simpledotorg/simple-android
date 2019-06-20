package org.simple.clinic.allpatientsinfacility

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult

class AllPatientsInFacilityView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), AllPatientsInFacilityUi {

  override fun showNoPatientsFound(facilityName: String) {
    TODO("not implemented")
  }

  override fun showPatients(facility: Facility, patientSearchResults: List<PatientSearchResult>) {
    TODO("not implemented")
  }
}
