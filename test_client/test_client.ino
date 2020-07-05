/*
 * 
 *   __   ____  _  _  ____  __     __   ____ 
 *  / _\ (_  _)/ )( \(  __)(  )   / _\ / ___)
 * /    \  )(  ) __ ( ) _) / (_/\/    \\___ \
 * \_/\_/ (__) \_)(_/(____)\____/\_/\_/(____/
 * 
 * LCD: https://learn.sparkfun.com/tutorials/sik-experiment-guide-for-arduino---v32/experiment-15-using-an-lcd
 * Bluetooth Package Parsing: http://osoyoo.com/2019/02/26/osoyoo-v2-robot-car-lesson-7-imitation-driving-with-bluetooth-app/
 * 
 */
 #include <LiquidCrystal.h>

#include <SoftwareSerial.h>
SoftwareSerial BLTSerial(4, 5);  //RX,TX

LiquidCrystal lcd(12,11,6,7,3,2);
  

#define MAX_PACKETSIZE 32    //Serial receive buffer

char buffUART[MAX_PACKETSIZE];
unsigned int buffUARTIndex = 0;
unsigned long preUARTTick = 0;


//WiFi / Bluetooth through the serial control
void do_Uart_Tick()
{

  char Uart_Date=0;
  if(BLTSerial.available()) 
  {
    size_t len = BLTSerial.available();
    uint8_t sbuf[len + 1];
    sbuf[len] = 0x00;
    BLTSerial.readBytes(sbuf, len);
    //parseUartPackage((char*)sbuf);
    memcpy(buffUART + buffUARTIndex, sbuf, len);//ensure that the serial port can read the entire frame of data
    buffUARTIndex += len;
    preUARTTick = millis();
    if(buffUARTIndex >= MAX_PACKETSIZE - 1) 
    {
      buffUARTIndex = MAX_PACKETSIZE - 2;
      preUARTTick = preUARTTick - 200;
    }
  }



  // buffUARTIndex > 0 means that a new message is being recieved
  
  if(buffUARTIndex > 0 && (millis() - preUARTTick >= 100))//APP send flag to modify the obstacle avoidance parameters
  { 
    // message is fully recieved and ready
    // buffUART has the full incoming message

    buffUART[buffUARTIndex] = 0x00;
    Uart_Date=buffUART[0];    
    buffUARTIndex = 0;

    lcd.setCursor(0,1);
    lcd.print("                ");
    lcd.setCursor(0,1);
    lcd.print(buffUART);

    Serial.print("[");
    Serial.print(buffUARTIndex);
    Serial.print("]\t'");
    Serial.print(buffUART); 
    Serial.println("'");

  }
}

//car motor control
void setup()
{
  Serial.begin(9600);//In order to fit the Bluetooth module's default baud rate, only 9600
  BLTSerial.begin(9600);

  lcd.begin(16, 2);
  lcd.clear();
  
  lcd.print("Hello, world!");
 
}

void loop()
{  
  do_Uart_Tick();
 
}
