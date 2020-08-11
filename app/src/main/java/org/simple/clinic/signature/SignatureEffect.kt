package org.simple.clinic.signature

import android.graphics.Bitmap
import java.io.File

sealed class SignatureEffect

object ClearSignature : SignatureEffect()

data class AcceptSignature(val bitmap: Bitmap?, val filePath : File) : SignatureEffect()

object CloseScreen : SignatureEffect()
