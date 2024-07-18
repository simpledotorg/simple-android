package org.simple.clinic.scanid.scannedqrcode

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class BlankScannedQRCodeResult : Parcelable

@Parcelize
data object AddToExistingPatient : BlankScannedQRCodeResult()

@Parcelize
data object RegisterNewPatient : BlankScannedQRCodeResult()
