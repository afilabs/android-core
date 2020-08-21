package com.support.location.engine.loader

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.support.location.engine.LocationEngine
import com.support.location.engine.LocationOptions
import com.support.location.engine.OnLocationUpdateListener

abstract class HardwareLoader(
        context: Context,
        options: LocationOptions,
        next: LocationLoader?
) : LocationLoader(context, next, options) {
    private val mCallbacks = hashMapOf<OnLocationUpdateListener, ILocationListener>()
    private val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var mLastLocationCallback: ILocationListener? = null
    abstract val provider: String

    override fun contains(listener: OnLocationUpdateListener): Boolean {
        return mCallbacks.containsKey(listener)
    }

    @SuppressLint("MissingPermission")
    override fun loadLastLocation(listener: OnLocationUpdateListener) {
        if (!hasPermission()) {
            nextLoadLast(listener)
            return
        }

        if (canReuseLastLocation(listener)) return

        if (!mLocationManager.isProviderEnabled(provider)) {
            nextLoadLast(listener)
            return
        }

        val location = mLocationManager.getLastKnownLocation(provider)

        if (location != null) {
            listener.onLocationUpdated(location)
            return
        }

        if (mLastLocationCallback == null) mLastLocationCallback = object : ILocationListener {
            override fun onLocationChanged(location: Location?) {
                mLocationManager.removeUpdates(mLastLocationCallback!!)
                if (location == null) nextLoadLast(listener)
                else notifyLocationUpdated(location, listener)
            }
        }

        mLocationManager.removeUpdates(mLastLocationCallback!!)
        mLocationManager.requestLocationUpdates(provider, options.interval, options.minDistance, mLastLocationCallback!!)
    }

    override fun getLastLocation(function: OnLocationUpdateListener) {
        loadLastLocation(function)
    }

    @SuppressLint("MissingPermission")
    override fun requestCallback(listener: OnLocationUpdateListener) {
        if (!hasPermission()) {
            nextRequest(listener)
            return
        }

        if (mCallbacks.containsKey(listener)) return
        if (!mLocationManager.isProviderEnabled(provider)) {
            nextRequest(listener)
            return
        }
        val callback = object : ILocationListener {
            override fun onLocationChanged(location: Location?) {
                if (location == null) nextRequest(listener)
                else notifyLocationUpdated(location, listener)
            }
        }
        mCallbacks[listener] = callback
        mLocationManager.requestLocationUpdates(provider, options.interval, options.minDistance, callback)
    }

    abstract fun hasPermission(): Boolean

    override fun removeCallback(listener: OnLocationUpdateListener): Boolean {
        val callback = mCallbacks.remove(listener) ?: return false
        mLocationManager.removeUpdates(callback)
        return true
    }
}

interface ILocationListener : LocationListener {

    override fun onProviderDisabled(provider: String?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}

class NetworkLoader(
    context: Context,
    next: LocationLoader? = null,
    options: LocationOptions = LocationOptions.DEFAULT
) : HardwareLoader(context, options, next) {
    override val provider: String
        get() = LocationManager.NETWORK_PROVIDER

    override fun hasPermission(): Boolean {
        return LocationEngine.isAllowed(context)
    }
}

class GPSLoader(
    context: Context,
    next: LocationLoader? = null,
    options: LocationOptions = LocationOptions.DEFAULT
) : HardwareLoader(context, options, next) {
    override val provider: String
        get() = LocationManager.GPS_PROVIDER

    override fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}