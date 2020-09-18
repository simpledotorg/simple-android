package org.simple.clinic.analytics

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import javax.inject.Inject

class NetworkCapabilitiesProvider @Inject constructor(private val application: Application) {

  fun activeNetworkCapabilities(): NetworkCapabilities? {
    val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val currentActiveNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      connectivityManager.activeNetwork
    } else {
      findCurrentActiveNetworkV21(connectivityManager)
    }

    return currentActiveNetwork?.let { network ->
      connectivityManager.getNetworkCapabilities(network)
    }
  }

  private fun findCurrentActiveNetworkV21(connectivityManager: ConnectivityManager): Network? {
    return connectivityManager
        .allNetworks
        .find { connectivityManager.getNetworkInfo(it)?.isConnected ?: false }
  }
}
