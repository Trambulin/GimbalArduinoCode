package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.os.Handler;
import android.renderscript.Matrix3f;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class GimbalControlActivity extends AppCompatActivity {
    Button testBt,phoneControlBt,sendingBt;
    TextView testTv;
    TextView bcTv;
    int ttt=0;
    PhoneSensors phoneSensors;
    private PhoneOrientation phoneOrientation;
    float before=0;
    float divider=360f/1008f;    //336
    byte ccc=0;
    boolean stabWithPhone=false;
    boolean sendingEulers=false;
    float pitch,roll,yaw;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            int gO=((BConnection) getApplicationContext()).getGimbal().goodOnes;
            int bO=((BConnection) getApplicationContext()).getGimbal().badOnes;
            testTv.setText(String.valueOf(gO));
            bcTv.setText(String.valueOf(bO));

//            int rate=(gO+bO)-ttt;
//            row1.setText(String.valueOf(rate));
//            ttt=gO+bO;
//            DCMphone.multiply(phoneOrientation.phoneRotMatrix);
            Matrix DCM = new Matrix();
            Matrix3f DCMgTemp = new Matrix3f();
            if(stabWithPhone){
                Matrix3f DCMpTemp=new Matrix3f();
                DCMpTemp.loadMultiply(phoneOrientation.currentDCMTemp,phoneOrientation.phoneRotMatrix);
                float[] phoneRelateInertiaOrient=DCMpTemp.getArray();
//                Matrix DCMphone=new Matrix();
//                DCMphone.setValues(phoneRelateInertiaOrient);
//                DCMphone.invert(DCMphone);
//                DCMphone.getValues(phoneRelateInertiaOrient);
                DCMpTemp=new Matrix3f(phoneRelateInertiaOrient);

                DCMgTemp.loadMultiply(((BConnection) getApplicationContext()).getGimbal().currentDCMOrient, ((BConnection) getApplicationContext()).getGimbal().gimbalRotMatrix);
                float[] gimbalRelateInertiaOrient=DCMgTemp.getArray();
                Matrix DCMgimbal=new Matrix();
                DCMgimbal.setValues(gimbalRelateInertiaOrient);
                DCMgimbal.invert(DCMgimbal);
                DCMgimbal.getValues(gimbalRelateInertiaOrient);
                DCMgTemp=new Matrix3f(gimbalRelateInertiaOrient);
                DCMgTemp.multiply(DCMpTemp);
                float[] phoneOrientValues=DCMgTemp.getArray();
                pitch= (float) Math.toDegrees(-Math.asin(phoneOrientValues[6]));
                roll= (float) Math.toDegrees(Math.atan2(phoneOrientValues[7],phoneOrientValues[8]));
                yaw= (float) Math.toDegrees(Math.atan2(phoneOrientValues[3],phoneOrientValues[0]));
            }
            else {
//                DCMgTemp=((BConnection) getApplicationContext()).getGimbal().currentDCMOrient;
                DCMgTemp.loadMultiply(((BConnection) getApplicationContext()).getGimbal().currentDCMOrient, ((BConnection) getApplicationContext()).getGimbal().gimbalRotMatrix);
//            DCMgTemp.multiply(((BConnection) getApplicationContext()).getGimbal().gimbalRotMatrix);
                float[] gimbalRelateInertiaOrient = DCMgTemp.getArray();
                DCM.setValues(gimbalRelateInertiaOrient);
                DCM.invert(DCM);
                float[] DCMValues = new float[9];
                DCM.getValues(DCMValues);
                pitch= (float) Math.toDegrees(-Math.asin(DCMValues[6]));
                roll= (float) Math.toDegrees(Math.atan2(DCMValues[7],DCMValues[8]));
                yaw= (float) Math.toDegrees(Math.atan2(DCMValues[3],DCMValues[0]));
            }
//            Matrix3f DCMpTemp=new Matrix3f();
//            DCMpTemp.loadMultiply(phoneOrientation.currentDCMTemp,phoneOrientation.phoneRotMatrix);
//            float[] phoneRelateInertiaOrient=DCMpTemp.getArray();
//            Matrix DCMphone=new Matrix();
//            DCMphone.setValues(phoneRelateInertiaOrient);
//            DCMphone.invert(DCMphone);
//            DCMphone.getValues(phoneRelateInertiaOrient);
//            pitch= (float) Math.toDegrees(-Math.asin(phoneRelateInertiaOrient[6]));
//            roll= (float) Math.toDegrees(Math.atan2(phoneRelateInertiaOrient[7],phoneRelateInertiaOrient[8]));
//            yaw= (float) Math.toDegrees(Math.atan2(phoneRelateInertiaOrient[3],phoneRelateInertiaOrient[0]));
//            ccc++;
//            byte[] tttm=new byte[1];
//            tttm[0]=ccc;
//            ((BConnection) getApplicationContext()).getConThread().write(tttm);
            if(sendingEulers)
                BLDCMotorValuesSend(pitch, roll, yaw);
            orient.addValues(pitch, roll, yaw);
            timerHandler.postDelayed(this, 30);
        }
    };

    Handler calibHandler=new Handler();
    Runnable calibRunnable=new Runnable() {
        @Override
        public void run() {
            if(!phoneOrientation.isCalibrating) {
                phoneOrientation.isCalibrating = true;
                ((BConnection) getApplicationContext()).getGimbal().isCalibrating=true;
                calibHandler.postDelayed(calibRunnable, 1000);
            }
            else {
                phoneOrientation.isCalibrating=false;
                ((BConnection) getApplicationContext()).getGimbal().isCalibrating=false;
                Toast.makeText(getApplicationContext(), "Calibration done.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    byte cc=1;
    GraphView orient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gimbal_control);

        testBt = (Button) findViewById(R.id.btTest);
        phoneControlBt = (Button) findViewById(R.id.row1);
        sendingBt=(Button)findViewById(R.id.row2);
        testTv = (TextView) findViewById(R.id.tvTest);
        bcTv = (TextView) findViewById(R.id.tv2);

        orient=(GraphView)findViewById(R.id.pitch);
        //this calculate works
//        Matrix testM = new Matrix();
//        float[] testVals = {1, 2, 0, 2, 1, 2, 0, 2, 1};
//        testM.setValues(testVals);
//        testM.invert(testM);
//        testM.getValues(testVals);
//        row1.setText(testVals[0] + "|" + testVals[1] + "|" + testVals[2]);
//        row2.setText(testVals[3] + "|" + testVals[4] + "|" + testVals[5]);
//        row3.setText(testVals[6] + "|" + testVals[7] + "|" + testVals[8]);

        phoneOrientation=new PhoneOrientation();
        phoneSensors=new PhoneSensors(getApplicationContext(),this);
        phoneSensors.sensorResume();

        testBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                byte[] tmp = new byte[1];
//                tmp[0] = cc;
//                cc++;
//                ((BConnection) v.getContext().getApplicationContext()).getConThread().write(tmp);
                final AlertDialog.Builder calibAlert  = new AlertDialog.Builder(GimbalControlActivity.this);
                calibAlert.setMessage("Calibration lasts ~2sec. During this time the devices(phone and gimbal) should stay as motionless as possible. The more motionless you can keep the devices the more accurate calibration you can get.");
                calibAlert.setTitle("Calibration information");
                calibAlert.setPositiveButton("I'm ready",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                calibHandler.postDelayed(calibRunnable, 1500);
                            }
                        });
                calibAlert.create().show();
            }
        });

        phoneControlBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(stabWithPhone)
                    stabWithPhone=false;
                else
                    stabWithPhone=true;
            }
        });

        sendingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendingEulers)
                    sendingEulers=false;
                else
                    sendingEulers=true;
            }
        });
        //timerHandler.postDelayed(timerRunnable, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.postDelayed(timerRunnable, 0);
//        byte[] tmp=new byte[1];
//        tmp[0]=75;
//        ((BConnection) getApplicationContext()).getConThread().write(tmp);

//        clientT.start();
//        sensorUses.sensorResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
//        byte[] tmp=new byte[1];
//        tmp[0]=75;
//        ((BConnection) getApplicationContext()).getConThread().write(tmp);

//        clientT.cancel();
//        sensorUses.sensorPause();
    }

    public PhoneOrientation getPhoneOrientation() {
        return phoneOrientation;
    }

    ///Values for the motors
    void gimbalOrientationReferenceToPhone(){
        Matrix3f DCMpTemp=new Matrix3f();
        DCMpTemp.loadMultiply(phoneOrientation.currentDCMTemp,phoneOrientation.phoneRotMatrix);
        float[] phoneRelateInertiaOrient=DCMpTemp.getArray();
        Matrix DCMphone=new Matrix();
        DCMphone.setValues(phoneRelateInertiaOrient);
        DCMphone.invert(DCMphone);
        DCMphone.getValues(phoneRelateInertiaOrient);
        DCMpTemp=new Matrix3f(phoneRelateInertiaOrient);

        Matrix3f DCMgTemp=new Matrix3f();
        DCMgTemp.loadMultiply(((BConnection) getApplicationContext()).getGimbal().currentDCMOrient, ((BConnection) getApplicationContext()).getGimbal().gimbalRotMatrix);
        float[] gimbalRelateInertiaOrient=DCMgTemp.getArray();
        Matrix DCMgimbal=new Matrix();
        DCMgimbal.setValues(gimbalRelateInertiaOrient);
        DCMgimbal.invert(DCMgimbal);
        DCMgimbal.getValues(gimbalRelateInertiaOrient);
        DCMgTemp=new Matrix3f(gimbalRelateInertiaOrient);
        DCMgTemp.multiply(DCMpTemp);
        float[] phoneOrientValues=DCMgTemp.getArray();
        pitch= (float) Math.toDegrees(-Math.asin(phoneOrientValues[6]));
        roll= (float) Math.toDegrees(Math.atan2(phoneOrientValues[7],phoneOrientValues[8]));
        yaw= (float) Math.toDegrees(Math.atan2(phoneOrientValues[3],phoneOrientValues[0]));
    }

    byte[] sendBig=new byte[5];
    int sBInd=0;
    void BLDCMotorValuesSend(float pitch, float roll, float yaw){
        pitch=pitch/divider;
        roll=roll/divider;
        yaw=yaw/divider;
        int pToSend=Math.round(pitch);
        int rToSend=Math.round(roll);
        int yToSend=Math.round(yaw);
        byte[] sendBytes=new byte[5];
        if (pToSend > 125){
            sendBytes[1]=125;
        }
        else if(pToSend<-125){
            sendBytes[1]=-125;
        }
        else{
            String tt=String.valueOf(pToSend);
            sendBytes[1]=Byte.valueOf(tt);
        }
        if(rToSend> 125){
            sendBytes[2]=125;
        }
        else if(rToSend<-125){
            sendBytes[2]=-125;
        }
        else{
            String tt=String.valueOf(rToSend);
            sendBytes[2]=Byte.valueOf(tt);
        }
        if(yToSend> 125){
            sendBytes[3]=125;
        }
        else if(yToSend<-125) {
            sendBytes[3] = -125;
        }
        else{
            String tt=String.valueOf(yToSend);
            sendBytes[3]=Byte.valueOf(tt);
        }
        sendBytes[0]=111;   //o character ASCII code
        byte sum= (byte) (sendBytes[1]+sendBytes[2]+sendBytes[3]);
        sendBytes[4]=sum;
        for (int i=0;i<5;i++){
            sendBig[sBInd++]=sendBytes[i];
        }
        if(sBInd>4) {
            sBInd=0;
            ((BConnection) getApplicationContext()).getConThread().write(sendBig);
        }
    }
}
