package org.simple.clinic.teleconsultlog.teleconsultrecord

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class TeleconsultRecordInfo(

    val recordedAt: Instant,

    val teleconsultationType: TeleconsultationType,

    val patientTookMedicines: Answer,

    val patientConsented: Answer,

    val medicalOfficerNumber: String?
) : Parcelable
