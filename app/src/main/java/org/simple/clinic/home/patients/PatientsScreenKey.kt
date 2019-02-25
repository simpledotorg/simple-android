package org.simple.clinic.home.patients

import io.reactivex.Observable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.PatientConfig
import org.simple.clinic.router.screen.FullScreenKey
import javax.inject.Inject

@Parcelize
class PatientsScreenKey : FullScreenKey {

  @IgnoredOnParcel
  @Inject
  lateinit var config: Observable<PatientConfig>

  init {
    TheActivity.component.inject(this)
  }

  @IgnoredOnParcel
  override val analyticsName = "Patients"

  override fun layoutRes(): Int = if (config.blockingFirst().showRecentPatients) {
    R.layout.screen_patients_v2
  } else {
    R.layout.screen_patients
  }
}
