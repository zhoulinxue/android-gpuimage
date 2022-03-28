package jp.co.cyberagent.android.gpuimage.sample.utils

import android.os.Handler
import android.os.Looper

abstract class BaseCameraLoader() : CameraLoader() {
    protected var switchCallback: SwitchCallback? = null
    protected var previewSuc = false
    protected var viewWidth: Int = 0
    protected var viewHeight: Int = 0
    private var switchDelay: Long = 25
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun switchCamera(callback: SwitchCallback) {
        switchCallback = callback
        switchCameraId()
        releaseCamera()
        handler.postDelayed(Runnable { setUpCamera() }, switchDelay)
    }

    abstract fun setUpCamera()

    abstract fun switchCameraId()

    override fun onResume(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        setUpCamera()
    }

    override fun onPause() {
        releaseCamera()
    }

    abstract fun releaseCamera()
}