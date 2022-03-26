package jp.co.cyberagent.android.gpuimage.sample.utils

import android.graphics.SurfaceTexture


abstract class CameraLoader {

    protected var onPreviewFrame: ((isFirstFram: Boolean, data: ByteArray, width: Int, height: Int) -> Unit)? = null

    abstract fun onResume(width: Int, height: Int)

    abstract fun onPause()

    abstract fun switchCamera(callback: SwitchCallback)

    abstract fun getCameraOrientation(): Int

    abstract fun hasMultipleCamera(): Boolean

    abstract fun setTexture(surfaceView: SurfaceTexture)

    fun setOnPreviewFrameListener(onPreviewFrame: (isFirstFram: Boolean,data: ByteArray, width: Int, height: Int) -> Unit) {
        this.onPreviewFrame = onPreviewFrame
    }

    abstract fun isFrontCamera(): Boolean
}