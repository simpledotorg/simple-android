package org.simple.clinic.mobius

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Observer
import com.spotify.mobius.android.LiveQueue

/**
 * Based on LiveData observeAsState extension
 */
@Composable
fun <V> LiveQueue<V>.DisposableViewEffect(onViewEffect: (V) -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(this, lifecycleOwner) {
    val observer = Observer<V> { onViewEffect(it) }
    setObserver(lifecycleOwner, observer)
    onDispose {
      clearObserver()
    }
  }
}
