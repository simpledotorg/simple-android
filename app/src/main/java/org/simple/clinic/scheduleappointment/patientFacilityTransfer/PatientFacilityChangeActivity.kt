package org.simple.clinic.scheduleappointment.patientFacilityTransfer

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.simple.clinic.R
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.util.LocaleOverrideContextWrapper
import java.util.Locale
import javax.inject.Inject

class PatientFacilityChangeActivity : AppCompatActivity() {

  @Inject
  lateinit var locale: Locale
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.screen_patient_facility_change)
  }

  override fun attachBaseContext(baseContext: Context) {
    val contextWithOverridenLocale = LocaleOverrideContextWrapper.wrap(baseContext, locale)
    super.attachBaseContext(ViewPumpContextWrapper.wrap(contextWithOverridenLocale))
  }

  fun updateFacilities(facilityItems: List<FacilityListItem>, updateType: FacilitiesUpdateType) {
    TODO("not implemented")
  }

  fun showToolbarWithSearchField() {
    TODO("not implemented")
  }

  fun showToolbarWithoutSearchField() {
    TODO("not implemented")
  }

  fun goBack() {
    TODO("not implemented")
  }

  fun showProgressIndicator() {
    TODO("not implemented")
  }

  fun hideProgressIndicator() {
    TODO("not implemented")
  }
}
