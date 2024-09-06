package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatinModel(
    val canPrescribeStatin: Boolean,
    val age: Int,
    val hasDiabetes: Boolean,
    val hasHadStroke: Boolean,
    val hasHadHeartAttack: Boolean,
) : Parcelable
