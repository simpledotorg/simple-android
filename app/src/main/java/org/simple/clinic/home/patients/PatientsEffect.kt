package org.simple.clinic.home.patients

sealed class PatientsEffect

object OpenEnterOtpScreen: PatientsEffect()

object OpenPatientSearchScreen: PatientsEffect()

object RefreshUserDetails: PatientsEffect()

object LoadUser: PatientsEffect()
