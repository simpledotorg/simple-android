package org.simple.clinic.scanid.scannedqrcode

import org.simple.clinic.patient.OngoingNewPatientEntry

sealed class ScannedQrCodeEffect

data class SaveNewOngoingPatientEntry(val entry: OngoingNewPatientEntry) : ScannedQrCodeEffect()

sealed class ScannedQrCodeViewEffect : ScannedQrCodeEffect()

data class SendBlankScannedQrCodeResult(val scannedQRCodeResult: BlankScannedQRCodeResult) : ScannedQrCodeViewEffect()
