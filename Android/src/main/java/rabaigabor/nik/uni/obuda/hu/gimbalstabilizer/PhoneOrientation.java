package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

import android.graphics.Matrix;
import android.renderscript.Matrix3f;

/**
 * Created by TramNote on 2016.01.21..
 */
public class PhoneOrientation {

    Vector e1,e2,e3;
    Vector accel,gyro,magnet,countMagnet;
    float gyroRate=0.005f;
    boolean hasAcc,hasMagnet;
    public Matrix DCMOrient;
    float[] phoneRotatorNumbers={0,0,-1,-1,0,0,0,1,0};
    public Matrix3f phoneRotMatrix=new Matrix3f(phoneRotatorNumbers);
    Matrix3f currentDCMTemp;
    public float pitch,roll,yaw;
    boolean isCalibrating=false;
    float calibCAcc =0, calibCMag=0;

    public PhoneOrientation() {
        e1 = new Vector(1, 0, 0);   //e1 a magnet, telefon "jobb" oldala az x irány
        countMagnet =new Vector((float)Math.cos(Math.toRadians(66.5)),0,(float)Math.sin(Math.toRadians(66.5)));
        e2 = new Vector(0, 1, 0);
        e3 = new Vector(0, 0, 1);
        gyro = new Vector(0, 0, 0);
        magnet=new Vector(1,0,0);
        accel=new Vector(0,0,1);
        hasAcc=false;
        hasMagnet=false;
        currentDCMTemp=new Matrix3f();
        DCMOrient=new Matrix();
        pitch=0;
        roll=0;
        yaw=0;
    }

    public void newAccel(Vector acc){
        if(isCalibrating){
            calibCAcc++;
            accel.setX(((calibCAcc -1)/ calibCAcc)*accel.getX()+((1/ calibCAcc)*acc.getX()));
            accel.setY(((calibCAcc - 1) / calibCAcc) * accel.getY() + ((1 / calibCAcc) * acc.getY()));
            accel.setZ(((calibCAcc - 1) / calibCAcc) * accel.getZ() + ((1 / calibCAcc) * acc.getZ()));
            e3.setX(accel.getX());
            e3.setY(accel.getY());
            e3.setZ(accel.getZ());
            e3.Normalize();
        }
        else {
            accel.setX(acc.getX());
            accel.setY(acc.getY());
            accel.setZ(acc.getZ());
            hasAcc = true;
            calibCAcc =0;
        }
    }

    public void newGyro(Vector gyro){
        if(!isCalibrating) {
            this.gyro.setX(gyro.getX() * gyroRate);
            this.gyro.setY(gyro.getY() * gyroRate);
            this.gyro.setZ(gyro.getZ() * gyroRate);
            orientationCalculate();
        }
    }

    public void newMagnet(Vector magnet){
        if(isCalibrating){
            calibCMag++;
            this.magnet.setX(((calibCMag -1)/ calibCMag)*this.magnet.getX()+((1/ calibCMag)*magnet.getX()));
            this.magnet.setY(((calibCMag - 1) / calibCMag) * this.magnet.getY() + ((1 / calibCMag) * magnet.getY()));
            this.magnet.setZ(((calibCMag - 1) / calibCMag) * this.magnet.getZ() + ((1 / calibCMag) * magnet.getZ()));
            countMagnet.setX(this.magnet.getX());
            countMagnet.setY(this.magnet.getY());
            countMagnet.setZ(this.magnet.getZ());
            countMagnet.Normalize();
        }
        else {
            this.magnet.setX(magnet.getX());
            this.magnet.setY(magnet.getY());
            this.magnet.setZ(magnet.getZ());
            hasMagnet = true;
            calibCMag=0;
        }
    }

    void orientationCalculate() {
        rotateDCM();
        if (hasAcc) {
            hasAcc = false;
            accel.Normalize();
            e3.setX(e3.getX() * 0.999f + accel.getX() * 0.001f);
            e3.setY(e3.getY() * 0.999f + accel.getY() * 0.001f);
            e3.setZ(e3.getZ() * 0.999f + accel.getZ() * 0.001f);
        }
        if (hasMagnet) {
            hasMagnet = false;
            magnet.Normalize();
            countMagnet.setX(countMagnet.getX() * 0.999f + magnet.getX() * 0.001f);
            countMagnet.setY(countMagnet.getY() * 0.999f + magnet.getY() * 0.001f);
            countMagnet.setZ(countMagnet.getZ() * 0.999f + magnet.getZ() * 0.001f);
        }
        e2 = e3.CrossProduct(countMagnet);
        e1 = e2.CrossProduct(e3);
        float[] matrixVals = {e1.getX(), e2.getX(), e3.getX(),
                              e1.getY(), e2.getY(), e3.getY(),
                              e1.getZ(), e2.getZ(), e3.getZ()};
        currentDCMTemp=new Matrix3f(matrixVals);
//        pitch= (float) Math.toDegrees(-Math.asin(e1.getZ()));
//        roll= (float) Math.toDegrees(Math.atan2(e2.getZ(),e3.getZ()));
//        yaw= (float) Math.toDegrees(Math.atan2(e1.getY(),e1.getX()));
    }

    void rotateDCM() {
        Vector de1 = gyro.CrossProduct(countMagnet);
        Vector de3 = gyro.CrossProduct(e3);
        countMagnet.setX(countMagnet.getX() - de1.getX());
        countMagnet.setY(countMagnet.getY() - de1.getY());
        countMagnet.setZ(countMagnet.getZ() - de1.getZ());
        e3.setX(e3.getX() - de3.getX());
        e3.setY(e3.getY() - de3.getY());
        e3.setZ(e3.getZ() - de3.getZ());
        countMagnet.Normalize();
        e3.Normalize();
    }

}
