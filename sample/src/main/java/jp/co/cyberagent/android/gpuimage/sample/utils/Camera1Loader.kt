@file:Suppress("DEPRECATION")

package jp.co.cyberagent.android.gpuimage.sample.utils

import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.view.Surface
import jp.co.cyberagent.android.gpuimage.util.ZCameraLog
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Camera1Loader(private val activity: Activity) : BaseCameraLoader() {

    private var cameraInstance: Camera? = null
    private var cameraFacing: Int = Camera.CameraInfo.CAMERA_FACING_BACK


    override fun getCameraOrientation(): Int {
        var roatation: Int = activity.windowManager.defaultDisplay.rotation
        val degrees = when (roatation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        ZCameraLog.d("getCameraOrientation, cameraFacing$cameraFacing , degrees$degrees, roatation$roatation")
        return if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (90 + degrees) % 360
        } else { // back-facing
            (90 - degrees) % 360
        }
    }

    override fun isFrontCamera(): Boolean {
        return (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT)
    }

    override fun hasMultipleCamera(): Boolean {
        return Camera.getNumberOfCameras() > 1
    }

    override fun setTexture(surfaceView: SurfaceTexture) {
        this.surfaceView = surfaceView
    }

    private var surfaceView: SurfaceTexture? = null;

    override fun setUpCamera() {
        val id = getCurrentCameraId()
        try {
            cameraInstance = getCameraInstance(id)
        } catch (e: IllegalAccessError) {
            ZCameraLog.e(TAG, "Camera not found")
            return
        }
        val parameters = cameraInstance!!.parameters


        if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }

        val pSizes = parameters.supportedPictureSizes
        //摄像头画面显示在Surface上
        val vSizes = parameters.supportedPreviewSizes
        val previewSize: Camera.Size = getSuitableSize(vSizes)
        ZCameraLog.e(TAG, "SupportedPreviewSize, width: " + previewSize.width + ", height: " + previewSize.height)
        val pictureSize: Camera.Size = getSuitableSize(pSizes)
        ZCameraLog.e(TAG, "SupportedPictrueSize, width: " + pictureSize.width + ", height: " + pictureSize.height)
        parameters.setPreviewSize(previewSize.width, previewSize.height) // 设置预览图像大小
        parameters.setPictureSize(pictureSize.width, pictureSize.height)

        // 设置图片格式
        parameters.pictureFormat = ImageFormat.JPEG
        // 设置JPG照片的质量
        parameters["jpeg-quality"] = 100
        cameraInstance!!.parameters = parameters
        cameraInstance!!.setPreviewCallback { data, camera ->
            if (data == null || camera == null) {
                return@setPreviewCallback
            }

            var isFirstFrame = !previewSuc
            previewSuc = true
            onPreviewFrame?.invoke(isFirstFrame, data, previewSize.width, previewSize.height)
        }

        switchCallback?.onSwitch(isFrontCamera())
        cameraInstance!!.setPreviewTexture(surfaceView)
        ZCameraLog.e("startPreview")
        cameraInstance!!.startPreview()
    }

    override fun switchCameraId() {
        cameraFacing = when (cameraFacing) {
            Camera.CameraInfo.CAMERA_FACING_FRONT -> Camera.CameraInfo.CAMERA_FACING_BACK
            Camera.CameraInfo.CAMERA_FACING_BACK -> Camera.CameraInfo.CAMERA_FACING_FRONT
            else -> return
        }
    }

    private fun getCurrentCameraId(): Int {
        val cameraInfo = Camera.CameraInfo()
        for (id in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(id, cameraInfo)
            if (cameraInfo.facing == cameraFacing) {
                return id
            }
        }
        return 0
    }

    private fun getCameraInstance(id: Int): Camera {
        return try {
            Camera.open(id)
        } catch (e: Exception) {
            throw IllegalAccessError("Camera not found")
        }
    }

    override fun releaseCamera() {
        cameraInstance?.setPreviewCallback(null)
        cameraInstance?.release()
        cameraInstance = null
        previewSuc = false
    }

    private var mRatio: CameraRatio = CameraRatio.SCANLE_4_3
    var screenP: Point? = null

    private fun getSuitableSize(sizes: List<Camera.Size>): Camera.Size {

        if (screenP == null) {
            screenP = CameraUtil.getScreenMetrics(activity)
        }
        var minDelta = Int.MAX_VALUE // 最小的差值，初始值应该设置大点保证之后的计算中会被重置
        // camera的宽度是大于高度的，这里要保证expectWidth > expectHeight
        val expectWidth: Int = max(screenP!!.x, screenP!!.y)
        val expectHeight: Int = min(screenP!!.x, screenP!!.y)
        var result = sizes[0]
        var hasSuitableSize = false
        for (i in sizes.indices) {
            val previewSize = sizes[i]
            // 找到一个与设置的分辨率差值最小的相机支持的分辨率大小
            if (previewSize.width * mRatio.widthRatio / mRatio.heightRatio === previewSize.height) {
                hasSuitableSize = true
                val delta: Int = abs(screenP!!.x - previewSize.height)
                if (minDelta >= delta) {
                    minDelta = delta
                    result = previewSize
                }
            } else if (!hasSuitableSize) {
                if (previewSize.width == expectWidth) {
                    if (abs(result.height - expectHeight)
                            > abs(previewSize.height - expectHeight)) {
                        result = previewSize
                    }
                } else if (previewSize.height == expectHeight) {
                    // 高度相等，则计算宽度最接近的Size
                    if (abs(result.width - expectWidth)
                            > abs(previewSize.width - expectWidth)) {
                        result = previewSize
                    }
                }
            }
        }
        return result // 默认返回与设置的分辨率最接近的预览尺寸
    }

    companion object {
        private const val TAG = "Camera1Loader"
    }
}