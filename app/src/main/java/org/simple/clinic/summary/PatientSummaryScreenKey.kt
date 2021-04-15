package org.simple.clinic.summary

import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.navigation.v2.ScreenKey
import java.time.Instant
import java.util.Objects
import java.util.UUID

@Parcelize
data class PatientSummaryScreenKey(
    val patientUuid: UUID,
    val intention: OpenIntention,
    // TODO(vs): 2019-10-18 Move this to the UI model when migrating to Mobius
    val screenCreatedTimestamp: Instant
) : ScreenKey(), Parcelable {

  @IgnoredOnParcel
  override val analyticsName = "Patient Summary"

  override fun instantiateFragment(): Fragment {
    return PatientSummaryScreen()
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other == null || this.javaClass != other.javaClass -> false
      else -> {
        val that = other as PatientSummaryScreenKey

        patientUuid == that.patientUuid && intention == that.intention
      }
    }
  }

  override fun hashCode(): Int {
    return Objects.hash(patientUuid, intention)
  }
}
