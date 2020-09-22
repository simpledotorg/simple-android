package org.simple.clinic.teleconsultlog.drugduration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class DrugDurationValidationResult : Parcelable

@Parcelize
object Blank : DrugDurationValidationResult()

@Parcelize
data class MaxDrugDuration(val maxDuration: Int) : DrugDurationValidationResult()

@Parcelize
object Valid : DrugDurationValidationResult()
