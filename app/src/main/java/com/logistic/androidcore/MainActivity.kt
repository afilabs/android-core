package com.logistic.androidcore

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import com.support.core.AppExecutors
import com.support.core.base.BaseActivity
import com.support.core.extension.lazyNone
import com.support.core.helpers.FileCache
import com.support.core.helpers.FileScale
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(R.layout.activity_main) {
    private val fileScale: FileScale by lazyNone { FileScale(this, FileCache(this, "Test-Photos")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnHelloWorld.setOnClickListener {
            permissionChecker.access(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ) {
                openCamera()
            }
        }
    }

    fun openGallery() {
        resultLife.onInstantSuccessResult(100) { data ->
            data?.data?.also {
                AppExecutors.diskIO.execute { fileScale.execute(it, true) }
            }
        }
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
        resultLife.onInstantSuccessResult(101) {
            try {
                AppExecutors.diskIO.execute { fileScale.execute(imageURI!!, true, true) }
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