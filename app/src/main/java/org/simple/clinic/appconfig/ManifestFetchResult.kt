package org.simple.clinic.appconfig

sealed class ManifestFetchResult

data class ManifestFetchSucceeded(val countries: List<Country>) : ManifestFetchResult()
