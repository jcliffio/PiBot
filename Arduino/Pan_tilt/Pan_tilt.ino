#include <Servo.h>

//Pan Tilt Variables ************************************
#define panPin 8
#define tiltPin 9
#define minPan 0
#define maxPan 180
#define minTilt 0
#define maxTilt 140

Servo pan;
Servo tilt;

boolean isPanTiltConnected;
//End Pan Tilt Variables ********************************

void setup()
{
  connectPanTilt();
}

void loop()
{
  for(int i = 0; i < maxPan; i++)
  {
    panRight(4);
  }
  for(int i = 180; i > minPan; i--)
  {
    panLeft(4);
  }
}

//Pan Tilt Section **************************************

void connectPanTilt()
{
  pan.attach(panPin);
  tilt.attach(tiltPin);
  resetPanTilt();
  isPanTiltConnected = 1;
}

void disconnectPanTilt()
{
  pan.detach();
  tilt.detach();
  isPanTiltConnected = 0;
}

void resetPanTilt()
{
  setPan(90);
  setTilt(90);
}

void setPan(int p)
{
  pan.write(p);
  delay(20);
}

void setTilt(int t)
{
  tilt.write(t);
  delay(20);
}

void tiltUp(int s)
{
  int oldPosition = tilt.read();
  int newPosition = oldPosition + s;
  if(newPosition > maxTilt)
  {
    return;
  }
  setTilt(newPosition);
}

void tiltDown(int s)
{
  int oldPosition = tilt.read();
  int newPosition = oldPosition - s;
  if(newPosition < minTilt)
  {
    return;
  }
  setTilt(newPosition);
}

void panLeft(int s)
{
  int oldPosition = pan.read();
  int newPosition = oldPosition - s;
  if(newPosition < minPan)
  {
    return;
  }
  setPan(newPosition);
}

void panRight(int s)
{
  int oldPosition = pan.read();
  int newPosition = oldPosition + s;
  if(newPosition > maxPan)
  {
    return;
  }
  setPan(newPosition);
}

//End Pan Tilt Section **********************************


