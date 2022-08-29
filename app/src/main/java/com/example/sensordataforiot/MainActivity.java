package com.example.sensordataforiot;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView acc_x,acc_y,acc_z, gyro_x, gyro_y, gyro_z,mag_x,mag_y,mag_z, compass_direction, compass_degree;
    float ax,ay,az,gx,gy,gz,degree;
    int direction;

    String clientId;
    MqttAndroidClient client;
    SensorEventListener accSensorEventListener, gyroSensorEventListener, magneticSensorEventListener;
    Sensor accSensor, gyroSensor, magneticSensor;
    SensorManager accSensorManager, gyroSensorManager, magneticSensorManager;

    private float[] floatAccelerometer = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    boolean isConnected = false;
    JSONObject jsonObject = new JSONObject();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // find id
        button = findViewById(R.id.update_button);
        acc_x = findViewById(R.id.accelerometer_x);
        acc_y = findViewById(R.id.accelerometer_y);
        acc_z = findViewById(R.id.accelerometer_z);
        gyro_x= findViewById(R.id.gyroscope_x);
        gyro_y= findViewById(R.id.gyroscope_y);
        gyro_z= findViewById(R.id.gyroscope_z);
        mag_x= findViewById(R.id.magnetic_x);
        mag_y= findViewById(R.id.magnetic_y);
        mag_z= findViewById(R.id.magnetic_z);
        compass_direction = findViewById(R.id.compass_direction);
        compass_degree = findViewById(R.id.compass_degree);

        // Acc Sensor
        accSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        accSensor = accSensorManager.getDefaultSensor(Sensor.T);

        accSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                acc_x.setText(String.format("%.2f",sensorEvent.values[0]));
                acc_y.setText(String.format("%.2f",sensorEvent.values[1]));
                acc_z.setText(String.format("%.2f",sensorEvent.values[2]));
                ax = sensorEvent.values[0];
                ay = sensorEvent.values[1];
                az = sensorEvent.values[2];
                try {
                    jsonObject.put("accelerometer_x",ax);
                    jsonObject.put("accelerometer_y",ay);
                    jsonObject.put("accelerometer_z",az);
                    jsonObject.put("time", System.currentTimeMillis() / 1000.0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                floatAccelerometer = sensorEvent.values;
                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatAccelerometer, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
                degree = (int) (floatOrientation[0]*180/3.14159 + 180);
                compass_degree.setText(String.valueOf(degree));

                // TODO: remove it
                if(isConnected){
                    Log.d("TEST", String.valueOf(jsonObject));
                    mqttPublish(jsonObject,client);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        // Gyro Sensor
        gyroSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                gyro_x.setText(String.format("%.2f",sensorEvent.values[0]));
                gyro_y.setText(String.format("%.2f",sensorEvent.values[1]));
                gyro_z.setText(String.format("%.2f",sensorEvent.values[2]));
                gx = sensorEvent.values[0];
                gy = sensorEvent.values[1];
                gz = sensorEvent.values[2];
                try {
                    jsonObject.put("gyro_x",gx);
                    jsonObject.put("gyro_y",gy);
                    jsonObject.put("gyro_z",gz);
                    jsonObject.put("time", System.currentTimeMillis() / 1000.0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(isConnected){
                    Log.d("TEST", String.valueOf(jsonObject));
                    mqttPublish(jsonObject,client);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        // Magnetic Sensor
        magneticSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = magneticSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magneticSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                mag_x.setText(String.format("%.2f",sensorEvent.values[0]));
                mag_y.setText(String.format("%.2f",sensorEvent.values[1]));
                mag_z.setText(String.format("%.2f",sensorEvent.values[2]));


                floatGeoMagnetic = sensorEvent.values;
                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatAccelerometer, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);
                degree = (int) (floatOrientation[0]*180/3.14159 + 180);

                if(degree >= 315 || degree < 45)
                    direction = 3;
                else if (degree >=45  & degree < 135)
                    direction = 4;
                else if (degree >=135  & degree < 225)
                    direction = 1;
                else if (degree >=225  & degree < 315)
                    direction = 2;

                compass_degree.setText(String.valueOf(degree));
                String d;
                if(direction == 1)
                    d = "N";
                else if(direction == 2)
                    d = "E";
                else if(direction == 3)
                    d = "S";
                else
                    d = "W";
                compass_direction.setText(d);
                try {
                    jsonObject.put("magnetic_x", sensorEvent.values[0]);
                    jsonObject.put("magnetic_y", sensorEvent.values[1]);
                    jsonObject.put("magnetic_z", sensorEvent.values[2]);
                    jsonObject.put("compass_degree",degree);
                    jsonObject.put("compass_direction", direction);
                    jsonObject.put("time", System.currentTimeMillis() / 1000.0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(isConnected){
                    Log.d("TEST", String.valueOf(jsonObject));
                    mqttPublish(jsonObject,client);
                }

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

        // Mqtt
        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://mqtt.eclipseprojects.io",
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("MQTT", "onSuccess");
                    isConnected = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTT", "onFailure");
                    isConnected = false;

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mqttdisconnect(client);
        isConnected = false;
    }

    void mqttPublish(JSONObject payload, MqttAndroidClient client)
    {
        String topic = "sensor_data";
//        String payload = "the payload";
        byte[] encodedPayload = new byte[0];
        try {
//            encodedPayload = payload..getBytes("UTF-8");
            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes("UTF-8"));
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    void mqttdisconnect(MqttAndroidClient client)
    {
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}