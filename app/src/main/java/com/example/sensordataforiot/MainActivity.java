package com.example.sensordataforiot;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView acc_x,acc_y,acc_z, gyro_x, gyro_y, gyro_z, compass_direction, compass_degree;

    String mqtt_broker, topic, client;
    SensorEventListener accSensorEventListener, gyroSensorEventListener, magneticSensorEventListener;
    Sensor accSensor, gyroSensor, magneticSensor;
    SensorManager accSensorManager, gyroSensorManager, magneticSensorManager;

    private float[] floatAccelerometer = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];
    float degree;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.update_button);
        acc_x = findViewById(R.id.accelerometer_x);
        acc_y = findViewById(R.id.accelerometer_y);
        acc_z = findViewById(R.id.accelerometer_z);
        gyro_x= findViewById(R.id.gyroscope_x);
        gyro_y= findViewById(R.id.gyroscope_y);
        gyro_z= findViewById(R.id.gyroscope_z);
        compass_direction = findViewById(R.id.compass_direction);
        compass_degree = findViewById(R.id.compass_degree);



        accSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                acc_x.setText(String.valueOf(sensorEvent.values[0]));
                acc_y.setText(String.valueOf(sensorEvent.values[1]));
                acc_z.setText(String.valueOf(sensorEvent.values[2]));

                floatAccelerometer = sensorEvent.values;
                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatAccelerometer, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
                degree = (float) (-floatOrientation[0]*180/3.14159);
                compass_degree.setText(String.valueOf(degree));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        gyroSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                gyro_x.setText(String.valueOf(sensorEvent.values[0]));
                gyro_y.setText(String.valueOf(sensorEvent.values[1]));
                gyro_z.setText(String.valueOf(sensorEvent.values[2]));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        magneticSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = magneticSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magneticSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                floatGeoMagnetic = sensorEvent.values;
                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatAccelerometer, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
                degree = (float) (-floatOrientation[0]*180/3.14159);

                compass_degree.setText(String.valueOf(degree));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };





    }


    @Override
    protected void onResume() {
        super.onResume();
        accSensorManager.registerListener(accSensorEventListener, accSensor, accSensorManager.SENSOR_DELAY_NORMAL);
        gyroSensorManager.registerListener(gyroSensorEventListener, gyroSensor, gyroSensorManager.SENSOR_DELAY_NORMAL);
        magneticSensorManager.registerListener(magneticSensorEventListener, magneticSensor, magneticSensorManager.SENSOR_DELAY_NORMAL);
    }
}