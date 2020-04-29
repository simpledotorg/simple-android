package org.simple.clinic.widgets.qrcodescanner

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class QrCodeScannerLifecycle : LifecycleOwner {

  private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

  init {
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  fun bindCamera() {
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
  }

  fun unBindCamera() {
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  fun destroyCamera() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
  }

  override fun getLifecycle(): Lifecycle = lifecycleRegistry
}