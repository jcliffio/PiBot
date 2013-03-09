#include <Wire.h>
#include <LSM303.h>

// Compass variables *********************
LSM303 compass;
// ***************************************

void setup()
{
  Serial.begin(9600);
  Wire.begin();
  setupCompass();
}

void loop()
{
  Serial.println(getCompassHeading());
}

// Compass functions ***********************************
void setupCompass()
{
  compass.init();
  compass.enableDefault();
  
  compass.m_min.x = -520; compass.m_min.y = -570; compass.m_min.z = -770;
  compass.m_max.x = +540; compass.m_max.y = +500; compass.m_max.z = 180;
}

int getCompassHeading()
{
  compass.read();
  return compass.heading();
}

// ******************************************************
