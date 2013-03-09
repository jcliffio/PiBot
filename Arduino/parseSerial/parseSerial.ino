
void setup()
{
  Serial.begin(115200);
}

void loop()
{
  String test = readSerial();
  if (test != "")
  {
    Serial.println(test);
  }
}

String readSerial()
{
  String readString = "";
  if(Serial.available() > 0)
  {
    delay(5);
    char c = Serial.read();
    while (c != '$')
    {
      c = Serial.read();
    }
    c = Serial.read();
    while (c != '#')
    {
      readString += c;
      c = Serial.read();
    }
  }
  return readString;
}
