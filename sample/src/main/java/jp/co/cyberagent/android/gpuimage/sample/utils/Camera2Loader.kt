package jp.co.cyberagent.android.gpuimage.sample.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.example.android.camera.utils.getPreviewOutputSize
import jp.co.cyberagent.android.gpuimage.GLTextureView

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Loader(private val activity: Activity) : BaseCameraLoader() {

    private var cameraInstance: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var cameraFacing: Int = CameraCharacteristics.LENS_FACING_BACK


    private val cameraManager: CameraManager by lazy {
        activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun getCameraOrientation(): Int {
        val degrees = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val cameraId = getCameraId(cameraFacing) ?: return 0
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: return 0
        return if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            (orientation + degrees) % 360
        } else { // back-facing
            (orientation - degrees) % 360
        }
    }

    override fun isFrontCamera(): Boolean {
        return (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT)
    }

    override fun hasMultipleCamera(): Boolean {
        return cameraManager.cameraIdList.size > 1
    }

    private var surfaceView: SurfaceTexture? = null;
    override fun setTexture(surfaceView: SurfaceTexture) {
        this.surfaceView = surfaceView;
    }

    @SuppressLint("MissingPermission")
    override fun setUpCamera() {
        val cameraId = getCameraId(cameraFacing) ?: return
        try {
            cameraManager.openCamera(cameraId, CameraDeviceCallback(), null)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Opening camera (ID: $cameraId) failed.")
        }
    }

    override fun switchCameraId() {
        cameraFacing = when (cameraFacing) {
            CameraCharacteristics.LENS_FACING_BACK -> CameraCharacteristics.LENS_FACING_FRONT
            CameraCharacteristics.LENS_FACING_FRONT -> CameraCharacteristics.LENS_FACING_BACK
            else -> return
        }
    }

    override fun releaseCamera() {
        previewSuc = false
        imageReader?.close()
        cameraInstance?.close()
        captureSession?.close()
        imageReader = null
        cameraInstance = null
        captureSession = null
    }

    private fun getCameraId(facing: Int): String? {
        return cameraManager.cameraIdList.find { id ->
            cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    private fun startCaptureSession() {
        val size = chooseOptimalSize()
        imageReader =
                ImageReader.newInstance(size.width, size.height, ImageFormat.YUV_420_888, 2).apply {
                    setOnImageAvailableListener({ reader ->
                        switchCallback?.onSwitch(isFrontCamera())
                        val image = reader?.acquireNextImage() ?: return@setOnImageAvailableListener
                        onPreviewFrame?.invoke(!previewSuc, image.generateNV21Data(), image.width, image.height)
                        previewSuc = true
                        image.close()
                    }, null)
                }

        try {
            cameraInstance?.createCaptureSession(
                    listOf(imageReader!!.surface),
                    CaptureStateCallback(),
                    null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to start camera session")
        }
    }

    private fun chooseOptimalSize(): Size {
//        val cameraId = getCameraId(cameraFacing) ?: return Size(0, 0)
//        val outputSizes = cameraManager.getCameraCharacteristics(cameraId)
//        return getPreviewOutputSize(activity.windowManager.defaultDisplay!!,
//                outputSizes,
//                this.javaClass,
//                ImageFormat.YUV_420_888)

        if (viewWidth == 0 || viewHeight == 0) {
            return Size(0, 0)
        }
        val cameraId = getCameraId(cameraFacing) ?: return Size(0, 0)
        val outputSizes = cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.YUV_420_888)

        val orientation = getCameraOrientation()
        val maxPreviewWidth = if (orientation == 90 or 270) viewHeight else viewWidth
        val maxPreviewHeight = if (orientation == 90 or 270) viewWidth else viewHeight

        return outputSizes?.filter {
            it.width <= maxPreviewWidth  && it.height <= maxPreviewHeight
        }?.maxBy {
            it.width * it.height
        } ?: Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)
    }

    private inner class CameraDeviceCallback : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraInstance = camera
            startCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            previewSuc = false
            camera.close()
            cameraInstance = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            previewSuc = false
            camera.close()
            cameraInstance = null
        }
    }

    private inner class CaptureStateCallback : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(TAG, "Failed to configure capture session.")
        }

        override fun onConfigured(session: CameraCaptureSession) {
            cameraInstance ?: return
            captureSession = session
            val builder = cameraInstance!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            builder.addTarget(imageReader!!.surface)
            try {
                session.setRepeatingRequest(builder.build(), null, null)
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to start camera preview.", e)
            }
        }
    }

    companion object {
        private const val TAG = "Camera2Loader"

        private const val PREVIEW_WIDTH = 480
        private const val PREVIEW_HEIGHT = 640
    }
}