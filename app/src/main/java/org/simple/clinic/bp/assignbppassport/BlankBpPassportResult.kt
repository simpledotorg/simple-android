package org.simple.clinic.bp.assignbppassport

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class BlankBpPassportResult : Parcelable

@Parcelize
object AddToExistingPatient : BlankBpPassportResult()

@Parcelize
object RegisterNewPatient : BlankBpPassportResult()
