/*
  Blink

  Turns an LED on for one second, then off for one second, repeatedly.

  Most Arduinos have an on-board LED you can control. On the UNO, MEGA and ZERO
  it is attached to digital pin 13, on MKR1000 on pin 6. LED_BUILTIN is set to
  the correct LED pin independent of which board is used.
  If you want to know what pin the on-board LED is connected to on your Arduino
  model, check the Technical Specs of your board at:
  https://www.arduino.cc/en/Main/Products

  modified 8 May 2014
  by Scott Fitzgerald
  modified 2 Sep 2016
  by Arturo Guadalupi
  modified 8 Sep 2016
  by Colby Newman

  This example code is in the public domain.

  http://www.arduino.cc/en/Tutorial/Blink
*/
//#include <SoftwareSerial.h>
//SoftwareSerial mySerial(2, 3); // RX, TX    ///For test

#include <SPI.h>
#include <RF24.h>
#include <BTLE.h>

RF24 radio(9, 10); // CE, CSN for nrf
BTLE btle(&radio);

int i;
byte Flag=2;     // flag for next data frame
byte buf[4]={0xf4,0x56,0x00,0xb6}; //to clear all mode(not used)
byte ECU_REQ[5]={0xf4,0x57,0x01,0x00,0xb4}; //req from ECM
byte ECU_RESP[64]={0x00};   //response from ECM

//Simple response for test
byte data[64] = {0xF4,0x92,0x01, 0x07,0x41,0x00,0x00,0x00,0x55,0x9A,0x6E,0xF8,0xF7,0xFF,0x1E,0x00,0x63,0x00,0x00,0x00,0x3F,0x9A,0x79,0x80,0x00,0x09,0x47,0x47,0x01,0x1E,0x19,0x23,0x23,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0x96,0x65,0x80,0x00,0x00,0xCE,0x00,0x00,0x00,0x00,0x84,0x00,0x08,0x00,0x01,0x01,0x00,0x20,0x06,0x00,0x00,0xA0,0x98,0xb1};

// the setup function runs once when you press reset or power the board
void setup() {

 //   mySerial.begin(8192);  //for test
  // initialize digital pin LED_BUILTIN as an output.
  //pinMode(LED_BUILTIN, OUTPUT);  //test led
 // digitalWrite(13, LOW);
    
  Serial.begin(8192);  //ALDL protocol

  i=0;  /// counter for recived Data
  
  btle.begin("");    // 8 chars max



}

// the loop function runs over and over again forever
void loop() {

  ///Send Request To ECU every 3 sec
  //i=0;
  memset(ECU_RESP, 0, sizeof(ECU_RESP));
  serial_rx_off();
  Serial.write(ECU_REQ,5);  // turn the LED on (HIGH is the voltage level)
  Serial.flush();           //Wait until tx finish
  serial_rx_on();
 // mySerial.write(data,64);  //for test
  //mySerial.flush();
  delay(500);  ///for test
  
  while (Serial.available() )  //read the ECU response 
 {
  ECU_RESP[i]=Serial.read();
  i++;
 }

 // i=63; //For test remove 
  //Serial.println(i);

  if(i==63) ///send it android Phone (not 64 becuse ardunino rx buffer is 63 byte)
  {
    i=0;
    //digitalWrite(13, HIGH);
  // Serial.println("we recive the Flowwing data:");
  // Serial.write(ECU_RESP,63);
  for(int c=1;c<=50;c++)
  {
    byte buf1[16]={0};


    if(Flag==2)
    { 
      Flag=1;
    buf1[0]=ECU_RESP[3+2];   //_malffw1
    buf1[1]=ECU_RESP[4+2];   //_malffw2
    buf1[2]=ECU_RESP[5+2];   //_malffw3
    buf1[3]=ECU_RESP[6+2];   //_malffw4
    buf1[4]=ECU_RESP[7+2];   //COOLANT TEMPERATURE
    buf1[5]=ECU_RESP[8+2];   //manifold temperature
    buf1[6]=ECU_RESP[9+2];   //A/D RESULT FROM MANIFOLD PRES. SENSOR INPUT
    buf1[7]=ECU_RESP[12+2];   //TPS voltage ADTHROTT
    buf1[8]=ECU_RESP[13+2];   //RPM
    buf1[9]=ECU_RESP[14+2];   //o2 Sensor Voltage
    buf1[10]=ECU_RESP[15+2];   //Speed in Miles per hour
    buf1[11]=ECU_RESP[20+2];    //BATTERY VOLTS
    buf1[12]=ECU_RESP[25+2];    ///IAC motor's current position
    buf1[13]=ECU_RESP[26+2];    //DESIRED ENGINE SPEED
    buf1[14]=ECU_RESP[39+2];    //AIR FUEL RATIO
    buf1[15]=ECU_RESP[58+2];    //o2 SENSOR READY

    
  // buf1[6]=ECU_RESP[22+3];    //SAP           
   // buf1[6]=ECU_RESP[23+3];    //SAP+1           
    //buf1[6]=ECU_RESP[24+3];    
    }
    else
    {
      Flag=2;
    buf1[0]=ECU_RESP[22+2];   //SAP
    buf1[1]=ECU_RESP[23+2];   //SAP+1 
    buf1[2]=ECU_RESP[24+2];   //Desired position of idle speed controller.
    buf1[3]=ECU_RESP[37+2];   //BPW 
    buf1[4]=ECU_RESP[38+2];   //BPW 
    buf1[5]=ECU_RESP[40+2];   //BLOCK LEARN MULTIPLIER 
    buf1[6]=ECU_RESP[41+2];   //BLMCELL        
    buf1[7]=ECU_RESP[42+2];   //INT           CLOSED LOOP INTEGRATOR  
    buf1[8]=ECU_RESP[60+2];   //Octan selsction  _stateByte11

    buf1[9]=ECU_RESP[48+2];   // _stateByte1 
    buf1[10]=ECU_RESP[49+2];  //_stateByte2 
    buf1[11]=ECU_RESP[51+2];  //_stateByte3 

    buf1[12]=ECU_RESP[52+2];   //_stateByte4 
    buf1[13]=ECU_RESP[54+2];   //_stateByte6   //Catalytic converter overtemp

      buf1[14]=ECU_RESP[59+2];  //_stateByte10  //iac RUN
      buf1[15]=ECU_RESP[56+2];  //_stateByte7  ///
         // buf1[13]=ECU_RESP[53+2];  //_stateByte5 
            // buf1[15]=ECU_RESP[57+2];  //_stateByte7    


    }

    
    
   nrf_service_data buf;
   buf.service_uuid = NRF_TEMPERATURE_SERVICE_UUID;
   buf.value = BTLE::to_nRF_Float(1.5);
  
  //if (!btle.advertise(0x16, &buf, sizeof(buf))) {
  if (!btle.advertise(Flag, &buf1, sizeof(buf1))) {

    
  }
  btle.hopChannel(); 

  

  
  } 
    // delay(1000); 
  
 

}
}


void serial_rx_off() {
  UCSR0B &= ~(_BV(RXEN0));  //disable UART RX
}

void serial_tx_off() {

   UCSR0B &= ~(_BV(TXEN0));  //disable UART TX
   delay(20);                 //allow time for buffers to flush
}

void serial_rx_on() {
  Serial.begin(8192);
}
