package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class OpenedFrom : Parcelable {

  @Parcelize
  object PatientsTabScreen : OpenedFrom()

  @Parcelize
  object InstantSearchScreen : OpenedFrom()

  sealed class EditPatientScreen : OpenedFrom() {

    @Parcelize
    object ToAddNHID : EditPatientScreen()

    @Parcelize
    object ToAddBpPassport : EditPatientScreen()
  }
}
