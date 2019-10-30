package org.simple.clinic.selectcountry

sealed class ManifestFetchError

object NetworkError : ManifestFetchError()
