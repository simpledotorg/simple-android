package org.simple.clinic.scanid.scannedqrcode

import org.simple.clinic.patient.OngoingNewPatientEntry

sealed class ScannedQrCodeEffect

data class SaveNewOngoingPatientEntry(val entry: OngoingNewPatientEntry) : ScannedQrCodeEffect()

data class SendBlankScannedQrCodeResult(val scannedQRCodeResult: BlankScannedQRCodeResult) : ScannedQrCodeEffect()
