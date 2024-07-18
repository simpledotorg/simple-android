package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class OpenedFrom : Parcelable {

  @Parcelize
  data object PatientsTabScreen : OpenedFrom()

  @Parcelize
  data object InstantSearchScreen : OpenedFrom()

  sealed class EditPatientScreen : OpenedFrom() {

    @Parcelize
    data object ToAddNHID : EditPatientScreen()

    @Parcelize
    data object ToAddBpPassport : EditPatientScreen()
  }
}
