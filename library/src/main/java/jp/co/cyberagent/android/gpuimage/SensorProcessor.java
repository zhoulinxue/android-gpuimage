package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;

public class SensorProcessor implements SensorEventListener {
    private Context mContext;
    
    SensorManager sm;
    private int currentRad;
    private  int rotation;

    public SensorProcessor(Context context) {
        this.mContext = context;
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }
        float[] values = event.values;
        float ax = values[0];
        float ay = values[1];
        double g = Math.sqrt(ax * ax + ay * ay);
        double cos = ay / g;
        if (cos > 1) {
            cos = 1;
        } else if (cos < -1) {
            cos = -1;
        }
        double rad = Math.acos(cos);
        if (ax < 0) {
            rad = 2 * Math.PI - rad;
        }

        int uiRot = rotation;
        double uiRad = Math.PI / 2 * uiRot;
        rad -= uiRad;
        currentRad = (int) (180 * rad / Math.PI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 获取 ORIENTATION_PORTRAIT 图片旋转角度
     *
     * @return
     */
    public  int getPortraitDegree(boolean isFrontCamera, int currentRad) {
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

    public void destory() {
        sm.unregisterListener(this);
    }
}
