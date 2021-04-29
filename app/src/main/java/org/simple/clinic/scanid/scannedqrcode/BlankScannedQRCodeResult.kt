package org.simple.clinic.scanid.scannedqrcode

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class BlankScannedQRCodeResult : Parcelable

@Parcelize
object AddToExistingPatient : BlankScannedQRCodeResult()

@Parcelize
object RegisterNewPatient : BlankScannedQRCodeResult()
