package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.nio.ByteBuffer;

/**
 * Created by TramNote on 2016.01.21..
 */
public class PhoneSensors implements SensorEventListener {
    SensorManager mSensorManager;
    Sensor accelerometer,gyroscope,magnetometer;
    GimbalControlActivity mainClass;
    Vector accel,gyro,magnet;
    PhoneOrientation phoneOrientation;
    long timeMeasurer=0;
    float avg=0,cc=0;

    public PhoneSensors(Context cont,GimbalControlActivity ui){
        mSensorManager=(SensorManager)cont.getSystemService(Context.SENSOR_SERVICE);
        gyroscope=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);;
        accelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magnetometer=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mainClass=ui;   //if i want refresh something on the ui
        accel=new Vector(0,0,1);
        magnet=new Vector(1,0,0);
        gyro=new Vector(0,0,0);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_GRAVITY) {
            accel.setX(event.values[0]);
            accel.setY(event.values[1]);
            accel.setZ(event.values[2]);
            mainClass.getPhoneOrientation().newAccel(accel);
        }
        else if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            gyro.setX(event.values[0]);
            gyro.setY(event.values[1]);
            gyro.setZ(event.values[2]);
            mainClass.getPhoneOrientation().newGyro(gyro);
        }
        else if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            magnet.setX(event.values[0]);
            magnet.setY(event.values[1]);
            magnet.setZ(event.values[2]);
            mainClass.getPhoneOrientation().newMagnet(magnet);

//            cc++;
//            avg=((cc-1)/cc)*avg+((1/cc)*(float)(System.currentTimeMillis() - timeMeasurer));
//            mainClass.setRow2(String.valueOf(avg));
//            timeMeasurer=System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void sensorResume() {
//        timeMeasurer=System.currentTimeMillis();
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

    }


    public void sensorPause(){
        mSensorManager.unregisterListener(this);
    }
}
