package jp.co.cyberagent.android.gpuimage.scales;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public abstract class BaseScale implements TextureScale {
    @Override
    public Buffer onScaleByParam(ScaleParam param) {
        float[] coordinate = param.getTargetCoordinate();
        float ratioHeight = param.getRatioHeight();
        float ratioWidth = param.getRatioWidth();
        FloatBuffer buffer = param.getBuffers();
        coordinate = getNewCoordinate(coordinate, ratioWidth, ratioHeight);
        buffer.clear();
        return buffer.put(coordinate).position(0);
    }

    protected abstract float[] getNewCoordinate(float[] coordinate, float ratioWidth, float ratioHeight);

}
