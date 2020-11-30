package com.logistic.androidcore

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.support.core.AppExecutors
import com.support.core.PermissionAccessibleImpl
import com.support.core.base.BaseActivity
import com.support.core.event.LocalEvent
import com.support.core.extension.call
import com.support.core.extension.lazyNone
import com.support.core.extension.subscribe
import com.support.core.helpers.BitmapLoader
import com.support.core.helpers.FileSaver
import com.support.core.helpers.ImageScale
import com.support.core.inject
import com.support.core.open
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(R.layout.activity_main) {
    companion object {
        val event = LocalEvent<Payload>()
    }

    private val fileCache: FileSaver by lazyNone { FileSaver(this) }

    private val fileScale: ImageScale by lazyNone { ImageScale(BitmapLoader(this)) }
    private val authRepo: AuthRepository by inject()
    private val permissionAccessible by lazyNone { PermissionAccessibleImpl(this) }
    private val testOpenCameraLiveData = MutableLiveData<Any>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnHelloWorld.text = "This is main activity"
//       testLocalEvent()
        testOpenCamera()
//        btnHelloWorld2.text = authRepo.name
//        testOpenGallery()
    }

    private fun testOpenCamera() {
        testOpenCameraLiveData.subscribe(this) {
            btnHelloWorld.setOnClickListener(permissionAccessible.access(0,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA
            ) {
                openCamera()
            })
        }
        testOpenCameraLiveData.call()
    }

    private fun testLocalEvent() {
        Log.e("File", "${event.value}")
        event.observeNotNull(this) {
            btnHelloWorld.text = it.value
        }
        event.observeNotNull(this) {
            btnHelloWorld2.text = it.value
        }
        btnHelloWorld.setOnClickListener {
            open<TestActivity> { }
        }
    }

    private fun testOpenGallery() {
        Handler().postDelayed({
            resultLife.onActivitySuccessResult(100) { data ->
                data?.data?.also {
                    AppExecutors.diskIO.execute {
                        fileCache.saveToCache(fileScale.scale(it), "test")
                    }
                }
            }
            btnHelloWorld.setOnClickListener {
                openGallery()
            }
        }, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
    }

    fun openCamera() {
        val imageURI = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.TITLE, "New Picture")
                    put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                })
        resultLife.onActivitySuccessResult(101) {
            try {
                AppExecutors.diskIO.execute {
                    try {
                        fileCache.saveToCache(fileScale.scale(imageURI!!), "test")
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageURI)
        }
        startActivityForResult(cameraIntent, 101)
    }
}