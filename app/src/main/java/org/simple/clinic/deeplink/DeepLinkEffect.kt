package org.simple.clinic.deeplink

sealed class DeepLinkEffect

object FetchUser : DeepLinkEffect()

object NavigateToSetupActivity : DeepLinkEffect()

object NavigateToMainActivity : DeepLinkEffect()
