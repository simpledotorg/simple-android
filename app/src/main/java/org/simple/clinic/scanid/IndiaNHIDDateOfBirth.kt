package org.simple.clinic.scanid

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Retention(RUNTIME)
@JsonQualifier
annotation class IndiaNHIDDateOfBirth{}
