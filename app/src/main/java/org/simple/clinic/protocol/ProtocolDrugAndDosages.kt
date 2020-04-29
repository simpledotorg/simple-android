package org.simple.clinic.protocol

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProtocolDrugAndDosages(val drugName: String, val drugs: List<ProtocolDrug>) : Parcelable
