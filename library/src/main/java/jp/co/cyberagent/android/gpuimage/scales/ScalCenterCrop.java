package jp.co.cyberagent.android.gpuimage.scales;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class ScalCenterCrop extends BaseScale {


    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }


    @Override
    public GPUImage.ScaleType scaleImpl() {
        return GPUImage.ScaleType.CENTER_CROP;
    }

    @Override
    protected float[] getNewCoordinate(float[] coordinate, float ratioWidth, float ratioHeight) {
        float distHorizontal = (1 - 1 / ratioWidth) / 2;
        float distVertical = (1 - 1 / ratioHeight) / 2;
        return new float[]{
                addDistance(coordinate[0], distHorizontal), addDistance(coordinate[1], distVertical),
                addDistance(coordinate[2], distHorizontal), addDistance(coordinate[3], distVertical),
                addDistance(coordinate[4], distHorizontal), addDistance(coordinate[5], distVertical),
                addDistance(coordinate[6], distHorizontal), addDistance(coordinate[7], distVertical),
        };
    }
}
