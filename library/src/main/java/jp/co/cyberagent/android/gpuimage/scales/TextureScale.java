package jp.co.cyberagent.android.gpuimage.scales;

import java.nio.Buffer;

import jp.co.cyberagent.android.gpuimage.GPUImage;

public interface TextureScale {
    public static int SCALE_BY_VERTEX = 0;
    public static int SCALE_BY_FRAGMENT = 1;

    Buffer onScaleByParam(ScaleParam param);

    GPUImage.ScaleType scaleImpl();
}
