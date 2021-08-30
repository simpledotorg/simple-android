package org.simple.clinic.appconfig

import org.simple.clinic.util.ResolvedError

sealed class ManifestFetchResult

data class FetchSucceeded(val countries: List<Country>) : ManifestFetchResult()

data class FetchError(val error: ResolvedError) : ManifestFetchResult()
