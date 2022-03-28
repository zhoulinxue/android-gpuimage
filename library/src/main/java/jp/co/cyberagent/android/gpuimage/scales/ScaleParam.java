package jp.co.cyberagent.android.gpuimage.scales;

import java.nio.FloatBuffer;

public class ScaleParam {
    private int ScaleImpl;
    private float[] targetCoordinate;
    private float ratioHeight;
    private float ratioWidth;
    private FloatBuffer buffers;

    public ScaleParam(float ratioHeight, float ratioWidth) {
        this.ratioHeight = ratioHeight;
        this.ratioWidth = ratioWidth;
    }

    public FloatBuffer getBuffers() {
        return buffers;
    }

    public void setBuffers(FloatBuffer buffers) {
        this.buffers = buffers;
    }

    public float getRatioHeight() {
        return ratioHeight;
    }

    public void setRatioHeight(float ratioHeight) {
        this.ratioHeight = ratioHeight;
    }

    public float getRatioWidth() {
        return ratioWidth;
    }

    public void setRatioWidth(float ratioWidth) {
        this.ratioWidth = ratioWidth;
    }

    public int getScaleImpl() {
        return ScaleImpl;
    }

    public void setScaleImpl(int scaleImpl) {
        ScaleImpl = scaleImpl;
    }

    public float[] getTargetCoordinate() {
        return targetCoordinate;
    }

    public void setTargetCoordinate(float[] targetCoordinate) {
        this.targetCoordinate = targetCoordinate;
    }
}
