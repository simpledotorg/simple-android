package org.simple.clinic.home

sealed class HomeScreenEffect

data object LoadCurrentFacility : HomeScreenEffect()

sealed class HomeScreenViewEffect : HomeScreenEffect()

data object OpenFacilitySelection : HomeScreenViewEffect()

data object ShowNotificationPermissionDenied : HomeScreenViewEffect()
