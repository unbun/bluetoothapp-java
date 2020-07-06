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
#include <string.h>

SoftwareSerial bleSerial(4, 5);  //RX,TX

LiquidCrystal lcd(12,11,6,7,3,2);
  
#define BUTTON_PIN 8
int deadZone = 5;
int buttonAxis = 0;


#define MAX_PACKETSIZE 32    //Serial receive buffer

char writeBuffer[MAX_PACKETSIZE];
const char* EOF_SUFFIX = "EOF";

char readBuffer[MAX_PACKETSIZE];
unsigned int readBufferIndex = 0;
unsigned long preUARTTick = 0;


// UART Bluetooth reads available serial messages
void uartReadTick()
{

  char Uart_Date=0;
  if(bleSerial.available()) 
  {
    size_t len = bleSerial.available();
    uint8_t sbuf[len + 1];
    sbuf[len] = 0x00;
    bleSerial.readBytes(sbuf, len);
    //parseUartPackage((char*)sbuf);
    memcpy(readBuffer + readBufferIndex, sbuf, len);//ensure that the serial port can read the entire frame of data
    readBufferIndex += len;
    preUARTTick = millis();
    if(readBufferIndex >= MAX_PACKETSIZE - 1) 
    {
      readBufferIndex = MAX_PACKETSIZE - 2;
      preUARTTick = preUARTTick - 200;
    }
  }



  // readBufferIndex > 0 means that a new message is being recieved
  
  if(readBufferIndex > 0 && (millis() - preUARTTick >= 100))//APP send flag to modify the obstacle avoidance parameters
  { 
    // message is fully recieved and ready
    // readBuffer has the full incoming message

    readBuffer[readBufferIndex] = 0x00;
    Uart_Date=readBuffer[0];    
    readBufferIndex = 0;

    lcd.setCursor(0,0);
    lcd.print("                ");
    lcd.setCursor(0,0);
    lcd.print(readBuffer);

    Serial.print("read in: ");
    Serial.println(readBuffer); 

  }
}

// UART Bluetooth writes to the serial buffer 
// (using random data and a button to trigger the writing)
void uartWriteTick(){
   int buttonState = digitalRead(BUTTON_PIN);
   
   if(buttonState == HIGH){
     buttonAxis = 0;
   }

   if(buttonState == LOW && buttonAxis < deadZone) {
    if(buttonAxis == 0){
      int rNum = (int) random(100);

      char message[MAX_PACKETSIZE - 4];
      sprintf(message, "Biodata: %d", rNum);

      lcd.setCursor(0,1);
      lcd.print("                ");
      lcd.setCursor(0,1);
      lcd.print(message);
      
      sprintf(writeBuffer, "%s%s", message, EOF_SUFFIX);
    } 

    // send data
    bleSerial.write(writeBuffer);

    Serial.print("wrote out: ");
    Serial.println(writeBuffer);

    buttonAxis++;
    deadZone = true;
   }   
}

void setup()
{
  Serial.begin(9600);//In order to fit the Bluetooth module's default baud rate, only 9600
  bleSerial.begin(9600);

  lcd.begin(16, 2);
  lcd.clear();
  
  lcd.print("Hello, world!");

  pinMode(BUTTON_PIN, INPUT);
  randomSeed(analogRead(0));
 
}

void loop()
{  
  uartReadTick();
  uartWriteTick();
 
}
