package com.tianfei.climbingstairs;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.text.DecimalFormat;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class TestActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private TextView mPressureVal;
    private TextView mAltitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        sensorManager = null;
        mPressureVal = (TextView)findViewById(R.id.presureVal);
        mAltitude = (TextView)findViewById(R.id.attitudeVal);


        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(mPressure == null)
        {
            mPressureVal.setText("can't do that without the pressrue sensor!");
            return;
        }

        Sensor mAccelerate = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener pressureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float sPV = event.values[0];
                mPressureVal.setText(String.valueOf(sPV));

                DecimalFormat df = new DecimalFormat("0.00");
                df.getRoundingMode();
                // 计算海拔
                double height = 44330000*(1-(Math.pow((Double.parseDouble(df.format(sPV))/1013.25),
                        (float)1.0/5255.0)));
                mAltitude.setText(df.format(height));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(pressureListener, mPressure,
                SensorManager.SENSOR_DELAY_NORMAL);

    }
}
