package org.simple.clinic.di.network

import java.util.concurrent.TimeUnit

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Timeout(
    val value: Long,
    val unit: TimeUnit
)

