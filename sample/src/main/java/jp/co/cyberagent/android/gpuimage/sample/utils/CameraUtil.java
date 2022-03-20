package jp.co.cyberagent.android.gpuimage.sample.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Surface;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.co.cyberagent.android.gpuimage.util.ZCameraLog;

/**
 * 相机工具类
 *
 * @author zhx
 * @version 1.0, 2015-11-15 下午5:23:57
 */
public class CameraUtil {
    /**
     * @param
     * @return
     * @throws Exception
     * @author zhx
     */
    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        ZCameraLog.i("Screen", "---Width = " + w_screen + " Height = " + h_screen
                + " densityDpi = " + dm.densityDpi);
        return new Point(w_screen, h_screen);
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        ZCameraLog.i("statusBarHeight", height + " ");
        return height;
    }

    /**
     * @param
     * @return int
     * @throws Exception
     * @author zhx
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * @param
     * @return int
     * @throws Exception
     * @author zhx
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 保存图片到相册
     *
     * @param context
     * @param bitmap
     * @return
     * @throws IOException
     */
    public static Uri saveImageData(Context context, Bitmap bitmap, String path) throws IOException {
        byte[] datas = ImageUtil.bitmap2Bytes(bitmap, false);
        return saveImageData(context, datas, path);
    }

    /**
     * 保存图片到相册
     *
     * @param context
     * @param data
     * @return
     * @throws IOException
     */
    public static Uri saveImageData(Context context, byte[] data, String path) throws IOException {
        Uri uri = null;
        String name = path + System.currentTimeMillis() + ".jpg";
        ZCameraLog.e("saveImageData, api > 29:  " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            ContentResolver contentResolver = context.getContentResolver();
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                OutputStream out = contentResolver.openOutputStream(uri);
                out.write(data);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File eFile = Environment.getExternalStorageDirectory();
            File mDirectory = new File(eFile.toString() + File.separator + path);
            if (!mDirectory.exists()) {
                mDirectory.mkdirs();
            }
            File imageFile = new File(mDirectory, name);
            FileOutputStream out;
            out = new FileOutputStream(imageFile);
            out.write(data);
            uri = toUri(context, imageFile.getAbsolutePath());
            updatePhotoAlbum(context, imageFile);//更新图库
            out.close();
        }
        return uri;
    }

    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    /**
     * 获取 ORIENTATION_PORTRAIT 图片旋转角度
     *
     * @return
     */
    public static int getPortraitDegree(boolean isFrontCamera, int currentRad) {
        int degree = 0;
        if (currentRad > 45 && currentRad < 135) {
            degree = 0;
        } else if (currentRad < 45 || currentRad > 315) {
            degree = isFrontCamera ? ExifInterface.ORIENTATION_ROTATE_270 : ExifInterface.ORIENTATION_ROTATE_90;
        } else if (currentRad > 225 && currentRad < 315) {
            degree = ExifInterface.ORIENTATION_ROTATE_180;
        } else {
            degree = isFrontCamera ? ExifInterface.ORIENTATION_ROTATE_90 : ExifInterface.ORIENTATION_ROTATE_270;
        }
        return degree;
    }


    /**
     * 兼容android 10
     * 更新图库
     *
     * @param mContext
     * @param file
     */
    private static void updatePhotoAlbum(Context mContext, File file) {
        MediaScannerConnection.scanFile(mContext.getApplicationContext(), new String[]{file.getAbsolutePath()}, new String[]{"image/jpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {

            }
        });

    }

    /**
     * 根据文件 地址 获取 uri
     *
     * @param context
     * @param filePath
     * @return
     */
    public static Uri toUri(Context context, String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".provider", new File(filePath));
        }
        return Uri.fromFile(new File(filePath));
    }

    /**
     * 计算 点击位置
     *
     * @param x
     * @param y
     * @param coefficient
     * @return
     */
    public static Rect calculateTapArea(float x, float y, float coefficient,float dirtX,float dirtY) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerY = 0;
        int centerX = 0;
        centerY = (int) (x / dirtX * 2000 - 1000);
        centerX = (int) (y / dirtY * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static int getCameraOri(int rotation, int cameraId) {
        int degrees = rotation * 90;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }

        // result 即为在camera.setDisplayOrientation(int)的参数
        int result;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
