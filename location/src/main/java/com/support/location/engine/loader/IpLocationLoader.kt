package com.support.location.engine.loader

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import com.google.gson.Gson
import com.support.location.engine.LocationOptions
import com.support.location.engine.OnLocationUpdateListener
import com.support.location.location
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*


class IpLocationLoader(context: Context, next: LocationLoader? = null, options: LocationOptions) : LocationLoader(context, next, options) {

    private val mExecutor = AsyncTask.THREAD_POOL_EXECUTOR
    private val mService = IpLocationService()

    override fun loadLastLocation(listener: OnLocationUpdateListener) {
        val lastLocation = this.lastLocation
        if (lastLocation != null) {
            listener.onLocationUpdated(lastLocation)
            return
        }
        mExecutor.execute {
            try {
                val hostInfo = mService.get()
                val location = hostInfo.location
                notifyLocationUpdated(location, listener)
            } catch (e: Throwable) {
                listener.onLocationUpdated(options.default.location)
            }
        }
    }

    override fun contains(listener: OnLocationUpdateListener): Boolean {
        return true
    }

    override fun requestCallback(listener: OnLocationUpdateListener) {
        loadLastLocation(listener)
    }

    override fun removeCallback(listener: OnLocationUpdateListener): Boolean {
        return true
    }

    override fun getLastLocation(function: OnLocationUpdateListener) {
        loadLastLocation(function)
    }
}

class HostIpInfo(private val lat: Double, private val lon: Double) {
    val location: Location
        get() = Location("").apply {
            latitude = lat
            longitude = lon
        }
}

class IpLocationService {
    companion object {
        private const val LOCATION_PROVIDER = "http://ip-api.com/json/?fields=lat,lon"
    }

    private var mTrustFactory: SSLSocketFactory? = null

    private val trustFactory: SSLSocketFactory
        get() {
            if (mTrustFactory == null) mTrustFactory = createTrustFactory()
            return mTrustFactory!!
        }

    @SuppressLint("TrustAllX509TrustManager")
    private fun createTrustFactory(): SSLSocketFactory {
        val sslContext: SSLContext = SSLContext.getInstance("SSL")

        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

        })

        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    fun get(): HostIpInfo {
        val server = URL(LOCATION_PROVIDER)
        val connection = server.openConnection() as HttpURLConnection
        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = trustFactory
            connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
        connection.connect()
        val json = connection.inputStream.bufferedReader().readText()
        return Gson().fromJson(json, HostIpInfo::class.java).also { connection.disconnect() }
    }
}