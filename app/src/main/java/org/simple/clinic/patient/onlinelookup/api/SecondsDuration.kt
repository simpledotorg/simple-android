package org.simple.clinic.patient.onlinelookup.api

import com.squareup.moshi.JsonQualifier
import kotlin.annotation.Retention

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class SecondsDuration
