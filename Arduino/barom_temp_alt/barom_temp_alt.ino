#include <Wire.h>
#include <Adafruit_BMP085.h>

// Barom variables ******************************

Adafruit_BMP085 bmp;
int currentPressure = 102600;

// **********************************************

void setup()
{
  
}

void loop()
{
  
}

// Barom functions ****************************************

void setupBarom()
{
  bmp.begin();
}



// ********************************************************
