package com.support.location.engine.loader

import android.content.Context
import android.os.AsyncTask
import com.support.location.engine.LocationOptions
import com.support.location.engine.OnLocationUpdateListener
import com.support.location.location
import java.io.InputStream
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import javax.xml.parsers.DocumentBuilderFactory


class IpLocationLoader(context: Context, next: LocationLoader?, options: LocationOptions) : LocationLoader(context, next, options) {

    private val mExecutor = AsyncTask.THREAD_POOL_EXECUTOR
    private val mService = IpLocationService()

    override fun loadLastLocation(listener: OnLocationUpdateListener) {
        mExecutor.execute {
            try {
                val hostInfo = mService.get()
//                listener.onLocationUpdated(hostInfo.location)
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

class HostIpInfo(stream: InputStream) {
    init {
        val factory = DocumentBuilderFactory.newInstance()
        val document = factory.newDocumentBuilder().parse(stream)
    }
}

class IpLocationService {
    companion object {
        private const val LOCATION_PROVIDER = "https://api.hostip.info/"
    }

    fun get(): HostIpInfo {
        val server = URL(LOCATION_PROVIDER)
        val connection = server.openConnection() as HttpsURLConnection

        val sslContext: SSLContext = SSLContext.getInstance("SSL")

        val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }

                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                }
        )

        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

        connection.sslSocketFactory = sslSocketFactory
        connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        connection.connect()
        return HostIpInfo(connection.inputStream).also { connection.disconnect() }
    }
}