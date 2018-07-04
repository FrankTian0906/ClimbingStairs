package com.tianfei.climbingstairs;


import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.Toast;
import android.content.Intent;

public class TestActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private TextView mPressureVal;
    private TextView mAltitude;
    private TextView mResult;
    private SensorEventListener pressureListener;
    private Sensor mPressure;
    private double height;

    private double firstPre = 0.0, secondPre;
    private double result;

    private final Timer timer = new Timer();
    private TimerTask task;

    public TestActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mResult = (TextView)findViewById(R.id.heightResult);
        mResult.setText(Double.toString(result));

        //PackageManager m = getPackageManager();
        //if(!m.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)){

       // }

        sensorManager = null;
        mPressureVal = (TextView)findViewById(R.id.presureVal);
        mAltitude = (TextView)findViewById(R.id.attitudeVal);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(mPressure == null)
        {
            Toast.makeText(this,"No pressure sensor!",Toast.LENGTH_LONG);
            return;
        }
        //Sensor mAccelerate = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        pressureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float sPV = event.values[0];
                mPressureVal.setText(String.valueOf(sPV));

                DecimalFormat df = new DecimalFormat("0.00");
                df.getRoundingMode();
                //altitude
                height = 44330000*(1-(Math.pow((Double.parseDouble(df.format(sPV))/1013.25),
                        (float)1.0/5255.0)));
                mAltitude.setText(df.format(height));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(pressureListener, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        //onResume();
        //onPause();

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };

        timer.schedule(task,1000,30000);
    }

    public double getHeight(){
        return height;
    }

    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            //System.out.println("Altitude: "+ getHeight());
            secondPre = getHeight();
            if(firstPre == 0.0)
                firstPre = secondPre;

            result =Math.abs(secondPre - firstPre);
            firstPre = secondPre;
            DecimalFormat df = new DecimalFormat("0.00");
            df.getRoundingMode();
            mResult.setText(df.format(result));
            System.out.println("Result: "+ result + "    First:" + firstPre +"   Second: " + secondPre);
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onResume() {
        sensorManager.registerListener(pressureListener, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(pressureListener!=null)
            sensorManager.unregisterListener(pressureListener);
            timer.cancel();
        super.onPause();
    }
}
