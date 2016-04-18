#include <SoftwareSerial.h>
#include "HMC5883L.h"
#include "I2Cdev.h"
#include "MPU6050.h"

//Code for Arduino MEGA!!!!

// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif

const int motorPS48[]={127,111,94,78,64,50,37,26,17,9,4,1,0,1,4,9,17,26,37,50,64,78,
94,111,127,144,160,176,191,205,218,229,238,245,251,254,255,254,251,245,238,229,218,205,191,176,160,144,127};
const int motorPS144[]={127,121,116,110,105,100,94,89,84,78,73,68,64,59,54,50,45,41,37,33,30,26,23,20,17,14,12,10,8,6,4,
3,2,1,0,0,0,0,0,1,2,3,4,6,8,10,12,14,17,20,23,26,30,33,37,41,45,50,54,59,64,68,73,78,84,89,94,100,105,110,116,121,127,133,138,144,149,
154,160,165,170,176,181,186,191,195,200,204,209,213,217,221,224,228,231,234,237,240,242,244,246,248,250,251,252,253,254,254,254,254,254,253,
252,251,250,248,246,244,242,240,237,234,231,228,224,221,217,213,209,204,200,195,191,186,181,176,170,165,160,154,149,144,138,133};

int currentStepPitchA = 0;  //0
int currentStepPitchB = 96; //32
int currentStepPitchC = 48; //16
int currentStepRollA = 0;   //0
int currentStepRollB = 96;  //32
int currentStepRollC = 48;  //16

const int motor1Pin1 =5;
const int motor1Pin2 =6;
const int motor1Pin3 =7;
const int motor2Pin1 =8;
const int motor2Pin2 =9;
const int motor2Pin3 =10;

long lastMotorDelayTime = 0;
const int motorDelayActual = 2;

int bluetoothTx = 51; // TX-O pin of bluetooth mate, Arduino D2
int bluetoothRx = 50; // RX-I pin of bluetooth mate, Arduino D3

SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);
// class default I2C address is 0x68
// specific I2C addresses may be passed as a parameter here
// AD0 low = 0x68 (default for InvenSense evaluation board)
// AD0 high = 0x69
MPU6050 accelgyro;
HMC5883L mag;
//MPU6050 accelgyro(0x69); // <-- use for AD0 high

int16_t ax, ay, az;
int16_t gx, gy, gz;
int16_t mx, my, mz;

byte sendB[48];

void setup() {
    // join I2C bus (I2Cdev library doesn't do this automatically)
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif
 
    // initialize serial communication
    // (38400 chosen because it works as well at 8MHz as it does at 16MHz, but
    // it's really up to you depending on your project)
    //Serial.begin(57600);
    pinMode(motor1Pin1, OUTPUT);
    pinMode(motor1Pin2, OUTPUT);
    pinMode(motor1Pin3, OUTPUT);
    pinMode(motor2Pin1, OUTPUT);
    pinMode(motor2Pin2, OUTPUT);
    pinMode(motor2Pin3, OUTPUT);
    bluetooth.begin(57600);
    
    // initialize device
    //Serial.write("Initializing I2C devices...");
    accelgyro.initialize();
    accelgyro.setI2CBypassEnabled(true);
    accelgyro.setDLPFMode(3);   //be van kapcsolva a DLPF-> 1kHz a gyro
    accelgyro.setRate(4);     //ennyivel osztja+1 az accel and gyro 1kHz frequencyt
    mag.initialize();

    // verify connection
    //Serial.write("Testing device connections...");
    //Serial.write(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");
    //Serial.write(mag.testConnection() ? "HMC5883L connection successful" : "HMC5883L connection failed");

    while(!bluetooth.available())
    {
      bluetooth.read();
      delay(10);
    }
    delay(55);
}

int mRateC=0;
int bIndex=0;
byte errorCounter=0;

byte dataFromBt=0;
int comeBytesInd=-1;
bool orientsComes=false;
byte orientBytes[3];
byte checkSum=0;

int beforeP=0;
int pitchDiff=0;
int pitch=0;
int beforeR=0;
int rollDiff=0;
int roll=0;

void loop() {
  while(bluetooth.available())
  {
    dataFromBt=bluetooth.read();
    //Serial.println(dataFromBt);
    if(comeBytesInd<0)
    {
      if(dataFromBt==111)
      {
        orientsComes=true;
        comeBytesInd++;
      }
      else
      {
        orientsComes=false;
      }
    }
    else
    {
      if(orientsComes)
      {
        if(comeBytesInd < 3)
        {
          orientBytes[comeBytesInd++]=dataFromBt;
          checkSum+=dataFromBt;
        }
        else
        {
          if(checkSum==dataFromBt)
          {
            //Serial.print(orientBytes[0]);
            //Serial.print(" ");
            //Serial.println(orientBytes[1]);
            
            //pitchDiff=orientBytes[0]-beforeP;
            //beforeP=orientBytes[0];
            //pitch=pitch+pitchDiff;
            //if(pitch>=235)
            //  pitch-=255;
            //else if(pitch<=-235)
            //  pitch+=255;
            if(orientBytes[0] > 150)
              pitch=orientBytes[0]-256;
            else
              pitch=orientBytes[0];
            pitch*=-1;  //*3 hogyha telefon kódot nem változtatom
            //rollDiff=orientBytes[1]-beforeR;
            //beforeR=orientBytes[1];
            //roll=roll+rollDiff;
            //if(roll>=235)
            //  roll-=255;
            //else if(roll<=-235)
            //  roll+=255;
            if(orientBytes[1] > 128)
              roll= orientBytes[1]-256;
            else
              roll=orientBytes[1];
            roll*=-1; //*3 hogyha telefon kódot nem változtatom
              
          }
          checkSum=0;
          comeBytesInd=-1;
        }
      }
      else
      {
        comeBytesInd++;
        if(comeBytesInd > 3){
          comeBytesInd=-1;
        }
      }
    }
  }
  if(accelgyro.getIntDataReadyStatus())
  {
    accelgyro.getMotion6(&ax, &ay, &az, &gx, &gy, &gz); //16 bit kettes komplemens, 1 bit=(1/131) fok
    sendB[bIndex++]=103;
    sendB[bIndex]=(byte)gx;
    errorCounter+=sendB[bIndex++];
    sendB[bIndex]=(byte)(gx>>8);
    errorCounter+=sendB[bIndex++];
    sendB[bIndex]=(byte)gy;
    errorCounter+=sendB[bIndex++];
    sendB[bIndex]=(byte)(gy>>8);
    errorCounter+=sendB[bIndex++];
    sendB[bIndex]=(byte)gz;
    errorCounter+=sendB[bIndex++];
    sendB[bIndex]=(byte)(gz>>8);
    errorCounter+=sendB[bIndex++];
    sendB[bIndex++]=errorCounter;
    errorCounter=0;
    if(bIndex>47)
    {
      bIndex=0;
      bluetooth.write(sendB,48);
    }
    mRateC++;
    if(mRateC>5)
    {
      mRateC=0;
      sendB[bIndex++]=97;
      sendB[bIndex]=(byte)ax;
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)(ax>>8);
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)ay;
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)(ay>>8);
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)az;
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)(az>>8);
      errorCounter+=sendB[bIndex++];
      sendB[bIndex++]=errorCounter;
      errorCounter=0;
      if(bIndex>47)
      {
        bIndex=0;
        bluetooth.write(sendB,48);
      }
      mag.getHeading(&mx, &my, &mz);
      sendB[bIndex++]=109;
      sendB[bIndex]=(byte)mx;
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)(mx>>8);
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)my;
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)(my>>8);
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)mz;
      errorCounter+=sendB[bIndex++];
      sendB[bIndex]=(byte)(mz>>8);
      errorCounter+=sendB[bIndex++];
      sendB[bIndex++]=errorCounter;
      errorCounter=0;
      if(bIndex>47)
      {
        bIndex=0;
        bluetooth.write(sendB,48);
      }
    }
  }
  if((millis() - lastMotorDelayTime) > motorDelayActual)
  {
    if(pitch<0)
    {
      pitch++;
      currentStepPitchA -=1;
      if(currentStepPitchA < 0) currentStepPitchA = 143;  //47
      currentStepPitchB -=1;
      if(currentStepPitchB < 0) currentStepPitchB = 143;
      currentStepPitchC -=1;
      if(currentStepPitchC < 0) currentStepPitchC = 143;
      analogWrite(motor1Pin1, motorPS144[currentStepPitchA]);
      analogWrite(motor1Pin2, motorPS144[currentStepPitchB]);
      analogWrite(motor1Pin3, motorPS144[currentStepPitchC]);
    }
    else if(pitch>0)
    {
      pitch--;
      currentStepPitchA +=1;
      if(currentStepPitchA > 143) currentStepPitchA = 0;
      currentStepPitchB +=1;
      if(currentStepPitchB > 143) currentStepPitchB = 0;
      currentStepPitchC +=1;
      if(currentStepPitchC > 143) currentStepPitchC = 0;
      analogWrite(motor1Pin1, motorPS144[currentStepPitchA]);
      analogWrite(motor1Pin2, motorPS144[currentStepPitchB]);
      analogWrite(motor1Pin3, motorPS144[currentStepPitchC]);
    }
    if(roll<0)
    {
      roll++;
      currentStepRollA +=1;
      if(currentStepRollA > 143) currentStepRollA = 0;
      currentStepRollB +=1;
      if(currentStepRollB > 143) currentStepRollB = 0;
      currentStepRollC +=1;
      if(currentStepRollC > 143) currentStepRollC = 0;
      analogWrite(motor2Pin1, motorPS144[currentStepRollA]);
      analogWrite(motor2Pin2, motorPS144[currentStepRollB]);
      analogWrite(motor2Pin3, motorPS144[currentStepRollC]);
    }
    else if(roll>0)
    {
      roll--;
      currentStepRollA -=1;
      if(currentStepRollA < 0) currentStepRollA = 143;
      currentStepRollB -=1;
      if(currentStepRollB < 0) currentStepRollB = 143;
      currentStepRollC -=1;
      if(currentStepRollC < 0) currentStepRollC = 143;
      analogWrite(motor2Pin1, motorPS144[currentStepRollA]);
      analogWrite(motor2Pin2, motorPS144[currentStepRollB]);
      analogWrite(motor2Pin3, motorPS144[currentStepRollC]);
    }
    lastMotorDelayTime = millis();
  }
}
