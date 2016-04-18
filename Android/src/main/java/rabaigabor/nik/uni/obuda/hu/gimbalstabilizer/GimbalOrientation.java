package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;


import android.graphics.Matrix;
import android.renderscript.Matrix3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by TramNote on 2016.01.14..
 */
public class GimbalOrientation {
    private String tmp1;
    byte[] fTT;
    int fIndex;
    public boolean isCalibrating=false;
    public Matrix DCMOrient;
    float calibCMag=0, calibCAcc=0, calibCGyro=0;

    int dataCounter;
    byte sensorType;
    float[] gimbalRotatorNumbers={0,0,1,0,-1,0,1,0,0};
    public Matrix3f gimbalRotMatrix=new Matrix3f(gimbalRotatorNumbers);
    public Matrix3f currentDCMOrient;

    public Vector e1,e2,e3;
    public Vector accel,gyro,magnet,countMagnet;
    float[] accelMeasures;
    public float[] gyroMeasures;
    float[] magnetMeasures;
    int medianLength=1;
    short[] accelBytes=new short[3];
    short[] accelXMedian=new short[medianLength]; short[] accelYMedian=new short[medianLength]; short[] accelZMedian=new short[medianLength];
    short[] gyroBytes=new short[3];
    short[] gyroXMedian=new short[medianLength]; short[] gyroYMedian=new short[medianLength]; short[] gyroZMedian=new short[medianLength];
    short[] magnetBytes=new short[3];
    short[] magnetXMedian=new short[medianLength]; short[] magnetYMedian=new short[medianLength]; short[] magnetZMedian=new short[medianLength];

    public GimbalOrientation(){
        setTmp1("5");
        e1=new Vector(1,0,0);
        countMagnet=new Vector((float)Math.cos(Math.toRadians(66.5)),0,(float)Math.sin(Math.toRadians(66.5)));
        e2=new Vector(0,1,0);
        e3=new Vector(0,0,1);
        DCMOrient=new Matrix();
        currentDCMOrient=new Matrix3f();
        fTT=new byte[4];
        fIndex=0;
        dataCounter=-1;
        sensorType=0;
        accel=new Vector(0,0,1);
        gyro=new Vector(0,0,0);
        countMagnet =new Vector((float)Math.cos(Math.toRadians(66.5)),0,(float)Math.sin(Math.toRadians(66.5)));
        magnet =new Vector((float)Math.cos(Math.toRadians(66.5)),0,(float)Math.sin(Math.toRadians(66.5)));

        accelMeasures=new float[3];
        gyroMeasures=new float[3];
        magnetMeasures=new float[3];
        gyroTimer=System.currentTimeMillis();
    }

    public boolean isTmpWrite=false;
    byte tester=0;
    public int goodOnes=0;
    public int badOnes=0;
    public int[] bytok=new int[5];
    int bIndex=0;

    String intek="";

    public void newData(byte[] tt,int byteNumber){
//        for (int i=0;i<byteNumber;i++){
//            bytok[bIndex++]=(int)tt[i];
//            if(bIndex>4)
//                bIndex=0;
//        }

//        for (int i=0;i<byteNumber;i++){
//            if(tt[i]==59){
//                try {
//                    tester++;
//                    bytok[bIndex]=Integer.parseInt(intek);
//                    if(bytok[bIndex++]==tester) {
//                        goodOnes++;
//                    }
//                    else {
//                        badOnes++;
//                    }
//                }
//                catch (Exception ex){
//                    badOnes++;
//                }
//                intek="";
//            }
//            else {
//                intek+=(char)tt[i];
//            }
//            if(bIndex>4)
//                bIndex=0;
//        }

//        for (int i = 0; i < byteNumber; i++) {
//            if (tt[i] == tester) {
//                goodOnes++;
//            } else {
//                badOnes++;
//            }
//            tester++;
//        }
        //fourBytesFloatProcess(tt,byteNumber);
        shortIntProcessWithError(tt,byteNumber);
    }

    byte[] sTT=new byte[2];
    byte errorCountedValue=0;
    byte errorGotValue=0;

    void shortIntProcessWithError(byte[] tt,int byteNumber){
        int i = 0;
        short s;
        while (i < byteNumber) {
            if (dataCounter > 2) {
                errorGotValue=tt[i++];
                dataCounter = -1;
                if(errorGotValue==errorCountedValue) {
                    goodOnes++;
                    if (sensorType == 97) { //a
                        accProcessByte();
                    } else if (sensorType == 103) {   //g
                        gyroProcessByte();
                    } else if (sensorType == 109) { //m
                        magnetProcessByte();
                    }
                }
                else
                    badOnes++;
                errorCountedValue=0;
            }
            if (dataCounter < 0 && i < byteNumber) {
                sensorType = tt[i++];
                dataCounter++;
            }
            while (fIndex < 2 && i < byteNumber) {
                errorCountedValue += tt[i];
                sTT[fIndex++] = tt[i++];
            }
            if (fIndex > 1) {
                fIndex = 0;
                s = ByteBuffer.wrap(sTT).order(ByteOrder.nativeOrder()).getShort();
                if (sensorType == 97) { //a
                    accelBytes[dataCounter] = s;
                } else if (sensorType == 103) {   //g
                    gyroBytes[dataCounter] = s;
                } else {
                    magnetBytes[dataCounter] = s;
                }
                dataCounter++;
            }
        }
    }

    void shortIntProcess(byte[] tt,int byteNumber){
        int i = 0;
        short s;
        while (i < byteNumber) {
            if (dataCounter < 0) {
                sensorType = tt[i++];
                dataCounter++;
            }
            while (fIndex < 2 && i < byteNumber) {
                sTT[fIndex++] = tt[i++];
            }
            if (fIndex > 1) {
                fIndex = 0;
                s = ByteBuffer.wrap(sTT).order(ByteOrder.nativeOrder()).getShort();
                if (sensorType == 97) { //a
                    accelBytes[dataCounter] = s;
                } else if (sensorType == 103) {   //g
                    gyroBytes[dataCounter] = s;
                } else {
                    magnetBytes[dataCounter] = s;
                }
                dataCounter++;
                if (dataCounter > 2) {
                    dataCounter = -1;
                    if (sensorType == 97) { //a
                        accProcessByte();
                    } else if (sensorType == 103) {   //g
                        gyroProcessByte();
                    } else {
                        magnetProcessByte();
                    }
                }
            }
        }
    }

    float comparer=1;

    void fourBytesFloatProcess(byte[] tt,int byteNumber) {
        int i = 0;
        float f;
        String sr;
        while (i < byteNumber) {
            if (dataCounter < 0) {
                sensorType = tt[i++];
                dataCounter++;
            }
            while (fIndex < 4 && i < byteNumber) {
                fTT[fIndex++] = tt[i++];
            }
            if (fIndex > 3) {
                fIndex = 0;
                f = ByteBuffer.wrap(fTT).order(ByteOrder.nativeOrder()).getFloat();
                sr = String.valueOf(f);

//                if (comparer == f) {  //simple float checker
//                    goodOnes++;
//                } else {
//                    badOnes++;
//                }
//                comparer++;


                if (sensorType == 97) { //a
                    accelMeasures[dataCounter] = f;
//                        if (dataCounter == 0) {
//                            if (f < -0.0001 && f > -0.5)
//                                goodOnes++;
//                            else
//                                badOnes++;
//                        } else if (dataCounter == 1) {
//                            if (f < -3.2 && f > -4.2)
//                                goodOnes++;
//                            else
//                                badOnes++;
//                        } else {
//                            if (f < -8.6 && f > -9.9)
//                                goodOnes++;
//                            else
//                                badOnes++;
//                        }
                } else if (sensorType == 103) {   //g
                    gyroMeasures[dataCounter] = f;
//                        if (dataCounter == 0) {
//                            if (f > -0.1 && f < 0.2)
//                                goodOnes++;
//                            else
//                                badOnes++;
//                        } else if (dataCounter == 1) {
//                            if (f > -0.1 && f < 0.2)
//                                goodOnes++;
//                            else
//                                badOnes++;
//                        } else {
//                            if (f < 0.1 && f > -0.2)
//                                goodOnes++;
//                            else
//                                badOnes++;
//                        }
                } else {
                    magnetMeasures[dataCounter] = f;
//                        if (f > -0.1 && f < 0.1) {
//                            goodOnes++;
//                        } else {
//                            if (dataCounter == 0) {
//                                if (f > -38 && f < -28)
//                                    goodOnes++;
//                                else
//                                    badOnes++;
//                            } else if (dataCounter == 1) {
//                                if (f > -11 && f < -1)
//                                    goodOnes++;
//                                else
//                                    badOnes++;
//                            } else {
//                                if (f > 34 && f < 45)
//                                    goodOnes++;
//                                else
//                                    badOnes++;
//                            }
//                        }
                }
                dataCounter++;
                if (dataCounter > 2) {
                    dataCounter = -1;
                    if (sensorType == 97) { //a
                        accProcess();
                    } else if (sensorType == 103) {   //g
                        //gyroTimer=System.currentTimeMillis()-gyroTimer;
                        //gyroFreq=((float)gyroTimer)/1000f;
                        goodOnes++;
                        gyroProcess();
                        //gyroTimer=System.currentTimeMillis();
                    } else {
                        magnetProcess();
                    }
                }
            }
        }
    }

    public String getTmp1() {
        return tmp1;
    }

    public void setTmp1(String tmp1) {
        this.tmp1 = tmp1;
    }

    boolean hasAcc=false;
    boolean hasMagnet=false;

    void accProcess(){
        //hardcoded calibration datas
        if(isCalibrating) {
            calibCAcc++;
            accel.setX((((calibCAcc -1)/ calibCAcc)*accel.getX())+((1/ calibCAcc)*(accelMeasures[0] + 1.5f)));
            accel.setY((((calibCAcc -1)/ calibCAcc)*accel.getY())+((1/ calibCAcc)*accelMeasures[1]));
            accel.setZ((((calibCAcc -1)/ calibCAcc)*accel.getZ())+((1/ calibCAcc)*((accelMeasures[2] + 4.7f) * 1.025f)));
            e3.setX(accel.getX());
            e3.setY(accel.getY());
            e3.setZ(accel.getZ());
            e3.Normalize();
        }
        else {
            accel.setX(accelMeasures[0] + 1.5f);
            accel.setY(accelMeasures[1]);
            accel.setZ((accelMeasures[2] + 4.7f) * 1.025f);
            calibCAcc=0;
            hasAcc = true;
        }
    }

    float accelScale=9.822f/16384f;

    void accProcessByte(){
        //hardcoded calibration datas
        if(isCalibrating) {
            calibCAcc++;
            accel.setX((((calibCAcc -1)/ calibCAcc)*accel.getX())+((1/ calibCAcc)*(((float)(accelBytes[0] - 2100))*1.0152439f*accelScale)));
            accel.setY((((calibCAcc -1)/ calibCAcc)*accel.getY())+((1/ calibCAcc)*(((float)(accelBytes[1]+50))*1.012158f*accelScale)));
            accel.setZ((((calibCAcc -1)/ calibCAcc)*accel.getZ())+((1/ calibCAcc)*(((float)(accelBytes[2] -350)) * accelScale)));
            e3.setX(accel.getX());
            e3.setY(accel.getY());
            e3.setZ(accel.getZ());
            e3.Normalize();
        } else {
            accel.setX(((float) (accelBytes[0] - 2100))*1.0152439f*accelScale);
            accel.setY(((float) (accelBytes[1]+50))*1.012158f*accelScale);
            accel.setZ(((float)(accelBytes[2] -350)) * accelScale);
            calibCAcc=0;
            hasAcc = true;
        }
    }

    void accProcessByteMedian(){
        //hardcoded calibration datas
        short saX=accelBytes[0];
        short saY=accelBytes[1];
        short saZ=accelBytes[2];
        if(accelXMedian[medianLength/2] < saX)
            medianBiggerNumber(accelXMedian,saX);
        else
            medianSmallerNumber(accelXMedian,saX);
        if(accelYMedian[medianLength/2] < saY)
            medianBiggerNumber(accelYMedian,saY);
        else
            medianSmallerNumber(accelYMedian,saY);
        if(accelZMedian[medianLength/2] < saZ)
            medianBiggerNumber(accelZMedian,saZ);
        else
            medianSmallerNumber(accelZMedian,saZ);
//        float xA=((float)(accelBytes[0] - 2100)) * 1.0152439f * accelScale;
//        float yA=((float)(accelBytes[1]+50)) * 1.012158f * accelScale;
//        float zA=((float)(accelBytes[2] - 350)) * accelScale;
        if(isCalibrating) {
            calibCAcc++;
            accel.setX((((calibCAcc -1)/ calibCAcc)*accel.getX())+((1/ calibCAcc)*(((float)(accelXMedian[medianLength/2] - 2100))*1.0152439f*accelScale)));
            accel.setY((((calibCAcc -1)/ calibCAcc)*accel.getY())+((1/ calibCAcc)*(((float)(accelZMedian[medianLength/2]+50))*1.012158f*accelScale)));
            accel.setZ((((calibCAcc -1)/ calibCAcc)*accel.getZ())+((1/ calibCAcc)*(((float)(accelYMedian[medianLength/2] -350)) * accelScale)));
            e3.setX(accel.getX());
            e3.setY(accel.getY());
            e3.setZ(accel.getZ());
            e3.Normalize();
        } else {
            accel.setX(((float)(accelXMedian[medianLength/2] - 2100))*1.0152439f*accelScale);
            accel.setY(((float)(accelYMedian[medianLength/2]+50))*1.012158f*accelScale);
            accel.setZ(((float)(accelZMedian[medianLength/2] - 350)) * accelScale);
            calibCAcc=0;
            hasAcc = true;
        }
    }

    int gXOffset=-580, gYOffset=-150, gZOffset=60;
    float gyroScale=1f/131f;

    void gyroProcessByte() {
        if(!isCalibrating) {
            gyro.setX((float)Math.toRadians(((float)(gyroBytes[0] - gXOffset))* gyroScale) * gyroFreq);
            gyro.setY((float)Math.toRadians(((float)(gyroBytes[1] - gYOffset))* gyroScale) * gyroFreq);
            gyro.setZ((float)Math.toRadians(((float)(gyroBytes[2] - gZOffset))* gyroScale) * gyroFreq);
            calibCGyro=0;
            orientationCalculate();
        }
        else{
            calibCGyro++;
            gXOffset=(int)((((calibCGyro -1)/ calibCGyro)*(float)gXOffset)+((1/ calibCGyro)*(float)gyroBytes[0]));
            gYOffset=(int)((((calibCGyro -1)/ calibCGyro)*(float)gYOffset)+((1/ calibCGyro)*(float)gyroBytes[1]));
            gZOffset=(int)((((calibCGyro -1)/ calibCGyro)*(float)gZOffset)+((1/ calibCGyro)*(float)gyroBytes[2]));
        }
    }

    void gyroProcessByteMedian() {
        short sgX=gyroBytes[0];
        short sgY=gyroBytes[1];
        short sgZ=gyroBytes[2];
        if(gyroXMedian[medianLength/2] < sgX)
            medianBiggerNumber(gyroXMedian,sgX);
        else
            medianSmallerNumber(gyroXMedian,sgX);
        if(gyroYMedian[medianLength/2] < sgY)
            medianBiggerNumber(gyroYMedian,sgY);
        else
            medianSmallerNumber(gyroYMedian,sgY);
        if(gyroZMedian[medianLength/2] < sgZ)
            medianBiggerNumber(gyroZMedian,sgZ);
        else
            medianSmallerNumber(gyroZMedian,sgZ);
        if(!isCalibrating) {
            gyro.setX((float) Math.toRadians(((float) (gyroXMedian[medianLength/2] - gXOffset)) * gyroScale) * gyroFreq);
            gyro.setY((float) Math.toRadians(((float) (gyroYMedian[medianLength/2] - gYOffset)) * gyroScale) * gyroFreq);
            gyro.setZ((float) Math.toRadians(((float) (gyroZMedian[medianLength/2] - gZOffset)) * gyroScale) * gyroFreq);
            calibCGyro=0;
            orientationCalculate();
        }
        else{
            calibCGyro++;
            gXOffset=(int)((((calibCGyro -1)/ calibCGyro)*(float)gXOffset)+((1/ calibCGyro)*(float)gyroXMedian[medianLength/2]));
            gYOffset=(int)((((calibCGyro -1)/ calibCGyro)*(float)gYOffset)+((1/ calibCGyro)*(float)gyroYMedian[medianLength/2]));
            gZOffset=(int)((((calibCGyro -1)/ calibCGyro)*(float)gZOffset)+((1/ calibCGyro)*(float)gyroZMedian[medianLength/2]));
        }
    }

    float magnetScale=0.92f;    //according to datasheet this convert to miniGauss unit (i dont need to convert)

    void magnetProcessByte() {
        //simple calibration without rotate
        if(isCalibrating){
            calibCMag++;
            this.magnet.setX(((calibCMag -1)/ calibCMag)*this.magnet.getX()+((1/ calibCMag)*((float)(magnetBytes[0] + 225))));
            this.magnet.setY(((calibCMag -1)/ calibCMag)*this.magnet.getY()+((1/ calibCMag)*((float)(magnetBytes[1] - 60)* 1.01797f)));
            this.magnet.setZ(((calibCMag - 1) / calibCMag) * this.magnet.getZ() + ((1 / calibCMag) * ((float)(magnetBytes[2] + 112) * 1.03561f)));
            countMagnet.setX(this.magnet.getX());
            countMagnet.setY(this.magnet.getY());
            countMagnet.setZ(this.magnet.getZ());
            countMagnet.Normalize();
        }
        else {
            magnet.setX((float)(magnetBytes[0] + 225));
            magnet.setY((float)(magnetBytes[1] - 60)* 1.01797f);
            magnet.setZ((float)(magnetBytes[2] + 112) * 1.03561f);
            calibCMag=0;
            hasMagnet = true;
        }
    }

    void magnetProcessByteMedian() {
        //simple calibration without rotate
        short smX=magnetBytes[0];
        short smY=magnetBytes[1];
        short smZ=magnetBytes[2];
        if(magnetXMedian[medianLength/2] < smX)
            medianBiggerNumber(magnetXMedian,smX);
        else
            medianSmallerNumber(magnetXMedian,smX);
        if(magnetYMedian[medianLength/2] < smY)
            medianBiggerNumber(magnetYMedian,smY);
        else
            medianSmallerNumber(magnetYMedian,smY);
        if(magnetZMedian[medianLength/2] < smZ)
            medianBiggerNumber(magnetZMedian,smZ);
        else
            medianSmallerNumber(magnetZMedian,smZ);
        if(isCalibrating){
            calibCMag++;
            this.magnet.setX(((calibCMag -1)/ calibCMag)*this.magnet.getX()+((1/ calibCMag)*((float)(magnetXMedian[medianLength/2] + 225))));
            this.magnet.setY(((calibCMag -1)/ calibCMag)*this.magnet.getY()+((1/ calibCMag)*((float)(magnetYMedian[medianLength/2] - 60)* 1.01797f)));
            this.magnet.setZ(((calibCMag - 1) / calibCMag) * this.magnet.getZ() + ((1 / calibCMag) * ((float)(magnetZMedian[medianLength/2] + 112) * 1.03561f)));
            countMagnet.setX(this.magnet.getX());
            countMagnet.setY(this.magnet.getY());
            countMagnet.setZ(this.magnet.getZ());
            countMagnet.Normalize();
        }
        else {
            magnet.setX((float)(magnetXMedian[medianLength/2] + 225));
            magnet.setY((float)(magnetYMedian[medianLength/2] - 60)* 1.01797f);
            magnet.setZ((float)(magnetZMedian[medianLength/2] + 112) * 1.03561f);
            calibCMag=0;
            hasMagnet = true;
        }
    }

    void medianBiggerNumber(short[] array, short value){
        int i=medianLength/2;
        short temp;
        boolean swapHappened=false;
        while(i<medianLength){
            if(array[i]<=value)
                i++;
            else{
                temp=array[i];
                array[i]=value;
                value=temp;
                swapHappened=true;
                i++;
            }
        }
        if(swapHappened){
            array[medianLength-1]=value;
        }
    }

    void medianSmallerNumber(short[] array, short value){
        int i=medianLength/2;
        short temp;
        boolean swapHappened=false;
        while(i>=0){
            if(array[i]>=value)
                i--;
            else{
                temp=array[i];
                array[i]=value;
                value=temp;
                swapHappened=true;
                i--;
            }
        }
        if(swapHappened){
            array[0]=value;
        }
    }

    public float gyroFreq=1f/200f;
    long gyroTimer;
    float gyroXOffset=0.02f,gyroYOffset=0.02f,gyroZOffset=-0.02f;

    void gyroProcess() {
        if(!isCalibrating) {
            gyro.setX((gyroMeasures[0] - gyroXOffset) * gyroFreq);
            gyro.setY((gyroMeasures[1] - gyroYOffset) * gyroFreq);
            gyro.setZ((gyroMeasures[2] - gyroZOffset) * gyroFreq);
            calibCGyro=0;
            orientationCalculate();
        }
        else{
            calibCGyro++;
            gyroXOffset=(((calibCGyro -1)/ calibCGyro)*gyroXOffset)+((1/ calibCGyro)*gyroMeasures[0]);
            gyroYOffset=(((calibCGyro -1)/ calibCGyro)*gyroYOffset)+((1/ calibCGyro)*gyroMeasures[1]);
            gyroZOffset=(((calibCGyro -1)/ calibCGyro)*gyroZOffset)+((1/ calibCGyro)*gyroMeasures[2]);
        }
    }

    void magnetProcess() {
        //simple calibration without rotate
        if(isCalibrating){
            calibCMag++;
            this.magnet.setX(((calibCMag -1)/ calibCMag)*this.magnet.getX()+((1/ calibCMag)*((magnetMeasures[0] + 6.45f) * 1.086f)));
            this.magnet.setY(((calibCMag -1)/ calibCMag)*this.magnet.getY()+((1/ calibCMag)*(magnetMeasures[1] + 16.9f)));
            this.magnet.setZ(((calibCMag -1)/ calibCMag)*this.magnet.getZ()+((1/ calibCMag)*((magnetMeasures[2] + 5.3f) * 1.007f)));
            countMagnet.setX(this.magnet.getX());
            countMagnet.setY(this.magnet.getY());
            countMagnet.setZ(this.magnet.getZ());
            countMagnet.Normalize();
        }
        else {
            magnet.setX((magnetMeasures[0] + 6.45f) * 1.086f);
            magnet.setY(magnetMeasures[1] + 16.9f);
            magnet.setZ((magnetMeasures[2] + 5.3f) * 1.007f);
            calibCMag=0;
            hasMagnet = true;
        }
    }

    void orientationCalculate() {
        rotateDCM();
        if (hasAcc) {
            hasAcc = false;
            accel.Normalize();
            e3.setX(e3.getX() * 0.99f + accel.getX() * 0.01f);
            e3.setY(e3.getY() * 0.99f + accel.getY() * 0.01f);
            e3.setZ(e3.getZ() * 0.99f + accel.getZ() * 0.01f);
        }
        if (hasMagnet) {
            hasMagnet = false;
            magnet.Normalize();
            countMagnet.setX(countMagnet.getX() * 0.99f + magnet.getX() * 0.01f);
            countMagnet.setY(countMagnet.getY() * 0.99f + magnet.getY() * 0.01f);
            countMagnet.setZ(countMagnet.getZ() * 0.99f + magnet.getZ() * 0.01f);
        }
        e2 = e3.CrossProduct(countMagnet);
        e1 = e2.CrossProduct(e3);
        float[] matrixVals = {e1.getX(), e2.getX(), e3.getX(),
                              e1.getY(), e2.getY(), e3.getY(),
                              e1.getZ(), e2.getZ(), e3.getZ()};
        DCMOrient.setValues(matrixVals);
        currentDCMOrient=new Matrix3f(matrixVals);
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
