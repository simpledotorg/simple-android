package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier

sealed class ScanResult : Parcelable

@Parcelize
data class ScannedId(val identifier: Identifier) : ScanResult()

@Parcelize
data class EnteredShortCode(val shortCode: String): ScanResult()
