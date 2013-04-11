
String serialIn;
int serialInLength;
int inputValues[6];

void setup()
{
  Serial.begin(115200);
}

void loop()
{
  readSerial();
  parseInputString();
  if (serialIn != "")
  {
    for(int i = 0; i < 6; i++)
    {
      Serial.println(inputValues[i]);
    }
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
  Serial.println(serialIn);
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
  inputValues[count] = tempString.toInt();
}
