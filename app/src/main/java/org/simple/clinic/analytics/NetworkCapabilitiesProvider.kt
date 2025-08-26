package org.simple.clinic.analytics

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import org.simple.clinic.analytics.NetworkConnectivityStatus.ACTIVE
import org.simple.clinic.analytics.NetworkConnectivityStatus.INACTIVE
import javax.inject.Inject

class NetworkCapabilitiesProvider @Inject constructor(private val application: Application) {

  fun activeNetworkCapabilities(): NetworkCapabilities? {
    val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val currentActiveNetwork =
        connectivityManager.activeNetwork

    return currentActiveNetwork?.let { network ->
      connectivityManager.getNetworkCapabilities(network)
    }
  }

  private fun findCurrentActiveNetworkV21(connectivityManager: ConnectivityManager): Network? {
    return connectivityManager
        .allNetworks
        .find { connectivityManager.getNetworkInfo(it)?.isConnected ?: false }
  }

  fun networkConnectivityStatus(): NetworkConnectivityStatus {
    val networkCapabilities = activeNetworkCapabilities() ?: return INACTIVE
    val isConnectedToNetwork = networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)
    val isConnectedToInternet =
        isConnectedToNetwork && networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED)

    return if (isConnectedToInternet) {
      ACTIVE
    } else {
      INACTIVE
    }
  }
}
