/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage.sample.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.sample.R
import jp.co.cyberagent.android.gpuimage.sample.utils.*
import jp.co.cyberagent.android.gpuimage.util.Rotation
import jp.co.cyberagent.android.gpuimage.util.ZCameraLog
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File

class CameraActivity : AppCompatActivity() {

    private val gpuImageView: GPUImageView by lazy { findViewById<GPUImageView>(R.id.surfaceView) }
    private val cameraLoader: CameraLoader by lazy {
//        if (Build.VERSION.SDK_INT < 21) {
        Camera1Loader(this)
//        } else {
//            Camera2Loader(this)
//        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        button_capture.setOnClickListener {
            saveSnapshot()
        }
        img_switch_camera.run {
            if (!cameraLoader.hasMultipleCamera()) {
                visibility = View.GONE
            }
            setOnClickListener {
                gpuImageView.filter = GPUImageGaussianBlurFilter()
                cameraLoader.switchCamera(object : SwitchCallback {
                    override fun onSwitch(isFrontCamera: Boolean) {
                        var rotation: Rotation = getRotation(cameraLoader.getCameraOrientation())
                        ZCameraLog.d("switchCamera,rotation$rotation")
                        gpuImageView.setRotation(rotation)
                        gpuImageView.setMirror(isFrontCamera)
                    }
                })
            }
        }

        cameraLoader.setOnPreviewFrameListener { isFirstFrame, data, width, height ->
            gpuImageView.updatePreviewFrame(isFirstFrame, data, width, height)
            if (isFirstFrame) {
                gpuImageView.filter = GPUImageFilter()
            }
        }

        cameraLoader.setTexture(gpuImageView.surfaceView)
        gpuImageView.setRotation(getRotation(cameraLoader.getCameraOrientation()))
        gpuImageView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY)
    }

    override fun onResume() {
        super.onResume()
        if (!hasCameraPermission() || !hasStoragePermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CameraActivity.REQUEST_CAMERA
            )
        } else {
            gpuImageView.doOnLayout {
                cameraLoader.onResume(it.width, it.height)
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


    override fun onPause() {
        cameraLoader.onPause()
        super.onPause()
    }

    private fun saveSnapshot() {
        val folderName = "GPUImage"
        var file: File = File(folderName)

        if (!file.exists()) {
            file.mkdirs()
        }

        val fileName = System.currentTimeMillis().toString() + ".jpg"
        gpuImageView.saveToPictures(folderName, fileName) {
            Glide.with(this).asBitmap().override(z_thumil_img.height).load(it).into(z_thumil_img)
            Toast.makeText(this, "$folderName/$fileName saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRotation(orientation: Int): Rotation {
        ZCameraLog.d("getRotation,orientation:$orientation")

        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == CameraActivity.REQUEST_CAMERA && grantResults.size == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            gpuImageView.doOnLayout {
                cameraLoader.onResume(it.width, it.height)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 1
    }
}
