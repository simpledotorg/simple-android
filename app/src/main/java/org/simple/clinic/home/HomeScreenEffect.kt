package org.simple.clinic.home

sealed class HomeScreenEffect

object LoadCurrentFacility : HomeScreenEffect()

sealed class HomeScreenViewEffect : HomeScreenEffect()

object OpenFacilitySelection : HomeScreenViewEffect()

object ShowNotificationPermissionDenied : HomeScreenViewEffect()
