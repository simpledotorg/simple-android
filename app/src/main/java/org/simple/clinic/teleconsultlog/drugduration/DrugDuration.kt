package org.simple.clinic.teleconsultlog.drugduration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class DrugDuration(
    val uuid: UUID,
    val name: String,
    val dosage: String?,
    val duration: String
) : Parcelable
