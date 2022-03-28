package jp.co.cyberagent.android.gpuimage.scales;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public class ScaleStanderd extends BaseScale {

    @Override
    public GPUImage.ScaleType scaleImpl() {
        return GPUImage.ScaleType.STANDERD;
    }

    @Override
    protected float[] getNewCoordinate(float[] coordinate, float ratioWidth, float ratioHeight) {
        return new float[]{
                coordinate[0] / ratioHeight, coordinate[1] / ratioWidth,
                coordinate[2] / ratioHeight, coordinate[3] / ratioWidth,
                coordinate[4] / ratioHeight, coordinate[5] / ratioWidth,
                coordinate[6] / ratioHeight, coordinate[7] / ratioWidth,
        };
    }
}
