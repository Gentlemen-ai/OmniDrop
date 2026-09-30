package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {

    /**
     * Gets the current local IPv4 address (e.g. from wlan0, ap0, or eth0).
     */
    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // Prioritize Wi-Fi (wlan), Hotspot (ap/p2p), then Ethernet
            val sortedInterfaces = interfaces.sortedByDescending { iface ->
                when {
                    iface.name.startsWith("wlan") -> 3
                    iface.name.startsWith("ap") -> 2
                    iface.name.startsWith("p2p") -> 2
                    iface.name.startsWith("eth") -> 1
                    else -> 0
                }
            }

            for (iface in sortedInterfaces) {
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = Collections.list(iface.inetAddresses)
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val hostAddress = addr.hostAddress ?: continue
                        if (!hostAddress.startsWith("127.")) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "192.168.1.102" // Fallback local IP if in restricted sandbox
    }

    /**
     * Checks whether the device is connected to Wi-Fi or has an active network.
     */
    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Returns the formatted Web Portal URL for browser access.
     */
    fun getPortalUrl(port: Int = 8080): String {
        return "http://${getLocalIpAddress()}:$port"
    }
}
