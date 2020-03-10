package org.simple.clinic.util

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ExpectUnsubscribed(val completables: Int = 0)
