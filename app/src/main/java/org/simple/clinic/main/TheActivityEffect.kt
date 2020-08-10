package org.simple.clinic.main

sealed class TheActivityEffect

object LoadAppLockInfo: TheActivityEffect()

object ClearLockAfterTimestamp: TheActivityEffect()

object ShowAppLockScreen: TheActivityEffect()
