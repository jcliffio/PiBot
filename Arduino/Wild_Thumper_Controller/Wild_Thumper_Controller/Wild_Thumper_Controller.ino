#include <Wire.h>
#include <LSM303.h>
#include <Adafruit_BMP085.h>
#include <Servo.h>
#include "IOpins.h"
#include "Constants.h"


//-------------------------------------------------------------- define global variables --------------------------------------------

unsigned int Volts;
unsigned int LeftAmps;
unsigned int RightAmps;
unsigned long chargeTimer;
unsigned long leftoverload;
unsigned long rightoverload;
int highVolts;
int startVolts;
int Leftspeed=0;
int Rightspeed=0;
byte Charged=1;                                               // 0=Flat battery  1=Charged battery
int Leftmode=1;                                               // 0=reverse, 1=brake, 2=forward
int Rightmode=1;                                              // 0=reverse, 1=brake, 2=forward
byte Leftmodechange=0;                                        // Left input must be 1500 before brake or reverse can occur
byte Rightmodechange=0;                                       // Right input must be 1500 before brake or reverse can occur
int LeftPWM;                                                  // PWM value for left  motor speed / brake
int RightPWM;                                                 // PWM value for right motor speed / brake
int data;
int servo[7];

//-----------------------------------------Serial variables-----------------------------------

String serialIn;
int serialInLength;
int inputValues[6];                              //Leftmode,Rightmode,LeftPWM,RightPWM,Pan,Tilt

String serialOut;
int serialOutLength;

//------------------------------------------pan/tilt variables---------------------------------

Servo camPan;
Servo camTilt;

int camPanVal;
int camTiltVal;

//------------------------------------------compass variables----------------------------------

//LSM303 compass;
int compassHeading;

//------------------------------------------barometer variables--------------------------------

//Adafruit_BMP085 barometer;
boolean baromConnected;
int temperature;
int pressure;
int altitude;


void setup()
{
  //------------------------------------------------------------ Initialize Servos ----------------------------------------------------
  camPan.attach(panPin);
  camTilt.attach(tiltPin);
  setCam(90,90);
  
  //------------------------------------------------------------ Initialize I/O pins --------------------------------------------------

  pinMode (Charger,OUTPUT);                                   // change Charger pin to output
  digitalWrite (Charger,1);                                   // disable current regulator to charge battery

  if (Cmode==1) 
  {
    Serial.begin(Brate);                                      // enable serial communications if Cmode=1
    Serial.flush();                                           // flush buffer
  } 
  //Serial.begin(57600);
  Wire.begin();
//  setupCompass();
}


void loop()
{
  //------------------------------------------------------------ Check battery voltage and current draw of motors ---------------------

  Volts=analogRead(Battery);                                  // read the battery voltage
  LeftAmps=analogRead(LmotorC);                               // read left motor current draw
  RightAmps=analogRead(RmotorC);                              // read right motor current draw

  //Serial.print(LeftAmps);
  //Serial.print("    ");
  //Serial.println(RightAmps);

  if (LeftAmps>Leftmaxamps)                                   // is motor current draw exceeding safe limit
  {
    analogWrite (LmotorA,0);                                  // turn off motors
    analogWrite (LmotorB,0);                                  // turn off motors
    leftoverload=millis();                                    // record time of overload
  }

  if (RightAmps>Rightmaxamps)                                 // is motor current draw exceeding safe limit
  {
    analogWrite (RmotorA,0);                                  // turn off motors
    analogWrite (RmotorB,0);                                  // turn off motors
    rightoverload=millis();                                   // record time of overload
  }

  if ((Volts<lowvolt) && (Charged==1))                        // check condition of the battery
  {                                                           // change battery status from charged to flat

    //---------------------------------------------------------- FLAT BATTERY speed controller shuts down until battery is recharged ----
    //---------------------------------------------------------- This is a safety feature to prevent malfunction at low voltages!! ------

    Charged=0;                                                // battery is flat
    highVolts=Volts;                                          // record the voltage
    startVolts=Volts;
    chargeTimer=millis();                                     // record the time

    digitalWrite (Charger,0);                                 // enable current regulator to charge battery
  }

  //------------------------------------------------------------ CHARGE BATTERY -------------------------------------------------------

  if ((Charged==0) && (Volts-startVolts>67))                  // if battery is flat and charger has been connected (voltage has increased by at least 1V)
  {
    if (Volts>highVolts)                                      // has battery voltage increased?
    {
      highVolts=Volts;                                        // record the highest voltage. Used to detect peak charging.
      chargeTimer=millis();                                   // when voltage increases record the time
    }

    if (Volts>batvolt)                                        // battery voltage must be higher than this before peak charging can occur.
    {
      if ((highVolts-Volts)>5 || (millis()-chargeTimer)>chargetimeout) // has voltage begun to drop or levelled out?
      {
        Charged=1;                                            // battery voltage has peaked
        digitalWrite (Charger,1);                             // turn off current regulator
      }
    } 
  }

  else
  {//----------------------------------------------------------- GOOD BATTERY speed controller opperates normally ----------------------


    SCmode();                                                // Serial mode via D0(RX) and D1(TX)

    setMotors();
    setCam(camPanVal, camTiltVal);
  }
}

void SCmode()
{   
  if (Serial.available()>1)                                   // command available
  {
    readSerial();
    parseInputString();
    assignInputValues();
    getOutputValues();
    packageString();
    writeSerial();
  }
}    

void readSerial()
{
  String readString = "";
  while(!Serial.available());
  char c = Serial.read();
  while (c != '$')
  {
    while(!Serial.available());
    c = Serial.read();
  }
  while(!Serial.available());
  c = Serial.read();
  while (c != '#')
  {
    readString += c;
    while(!Serial.available());
    c = Serial.read();
  }
  
  serialIn = readString;
  serialInLength = readString.length();
}

void parseInputString()
{
  int count = 0;
  String tempString = "";
  
  for (int i = 0; i < serialInLength; i++)
  {
    if (serialIn.charAt(i) == ',')
    {
      inputValues[count] = tempString.toInt();
      count++;
      tempString = "";
    }
    else
    {
      tempString += serialIn.charAt(i);
    }
  }
}

void assignInputValues()
{
  Leftmode = inputValues[0];
  LeftPWM = inputValues[1];
  Rightmode = inputValues[2];
  RightPWM = inputValues[3];
  camPanVal = inputValues[4];
  camTiltVal = inputValues[5];
}

void getOutputValues()
{
  
}
void packageString()
{
  
}
void writeSerial()
{
  
}

void setMotors()
{
  if (Charged==1)                                           // Only power motors if battery voltage is good
  {
    if ((millis()-leftoverload)>overloadtime)             
    {
      switch (Leftmode)                                     // if left motor has not overloaded recently
      {
      case 2:                                               // left motor forward
        analogWrite(LmotorA,0);
        analogWrite(LmotorB,LeftPWM);
        break;

      case 1:                                               // left motor brake
        analogWrite(LmotorA,LeftPWM);
        analogWrite(LmotorB,LeftPWM);
        break;

      case 0:                                               // left motor reverse
        analogWrite(LmotorA,LeftPWM);
        analogWrite(LmotorB,0);
        break;
      }
    }
    if ((millis()-rightoverload)>overloadtime)
    {
      switch (Rightmode)                                    // if right motor has not overloaded recently
      {
      case 2:                                               // right motor forward
        analogWrite(RmotorA,0);
        analogWrite(RmotorB,RightPWM);
        break;

      case 1:                                               // right motor brake
        analogWrite(RmotorA,RightPWM);
        analogWrite(RmotorB,RightPWM);
        break;

      case 0:                                               // right motor reverse
        analogWrite(RmotorA,RightPWM);
        analogWrite(RmotorB,0);
        break;
      }
    } 
  }
  else                                                      // Battery is flat
  {
    analogWrite (LmotorA,0);                                // turn off motors
    analogWrite (LmotorB,0);                                // turn off motors
    analogWrite (RmotorA,0);                                // turn off motors
    analogWrite (RmotorB,0);                                // turn off motors
  }
}

//---------------------------------------------------------Pan/Tilt functions------------------------------
void attachCam()
{
  camPan.attach(panPin);
  camTilt.attach(tiltPin);
  setCam(90,90);
}

void setCam(int p, int t)
{
  setPan(p);
  setTilt(t);
}

void setPan(int p)
{
  if(p < minPan || p > maxPan)
  {
    return;
  }
  camPan.write(p);
  delay(15);
}

void setTilt(int t)
{
  if(t < minTilt || t > maxTilt)
  {
    return;
  }
  camTilt.write(t);
  delay(15);
}

//--------------------------------------------------------Sensor functions---------------------------------
//void setupSensors()
//{
//  setupCompass();
//  baromConnected = setupBarom();
//}

//void setupCompass()
//{
//  compass.init();
//  compass.enableDefault();
//  
//  compass.m_min.x = -520; compass.m_min.y = -570; compass.m_min.z = -770;
//  compass.m_max.x = +540; compass.m_max.y = +500; compass.m_max.z = 180;
//}

//boolean setupBarom()
//{
//  if (!barometer.begin())
//  {
//    return false;
//  }
//  return true;
//}

//int getCompassHeading()
//{
//  compass.read();
//  return compass.heading();
//}
