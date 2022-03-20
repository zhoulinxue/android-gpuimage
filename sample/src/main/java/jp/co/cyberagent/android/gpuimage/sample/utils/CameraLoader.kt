package jp.co.cyberagent.android.gpuimage.sample.utils

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import jp.co.cyberagent.android.gpuimage.GLTextureView


abstract class CameraLoader {

    protected var onPreviewFrame: ((data: ByteArray, width: Int, height: Int) -> Unit)? = null

    abstract fun onResume(width: Int, height: Int)

    abstract fun onPause()

    abstract fun switchCamera()

    abstract fun getCameraOrientation(): Int

    abstract fun hasMultipleCamera(): Boolean

    abstract fun setTexture(surfaceView: SurfaceTexture)

    fun setOnPreviewFrameListener(onPreviewFrame: (data: ByteArray, width: Int, height: Int) -> Unit) {
        this.onPreviewFrame = onPreviewFrame
    }
}