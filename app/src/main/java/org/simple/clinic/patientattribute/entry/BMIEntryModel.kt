package org.simple.clinic.patientattribute.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class BMIEntryModel(
    val patientUUID: UUID,
    val height: String = "",
    val weight: String = ""
) : Parcelable {
    companion object {
        fun default(patientUUID: UUID) = BMIEntryModel(
            patientUUID = patientUUID,
        )
    }

    fun heightChanged(height: String): BMIEntryModel =
        copy(height = height)

    fun weightChanged(weight: String): BMIEntryModel =
        copy(weight = weight)

    fun deleteWeightLastDigit(): BMIEntryModel = if (weight.isNotEmpty())
        copy(weight = weight.unsafeDropLastChar())
    else
        this

    private fun String.unsafeDropLastChar(): String =
        this.substring(0, this.length - 1)
}
