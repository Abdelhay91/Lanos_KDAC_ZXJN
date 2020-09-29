#include <LiquidCrystal.h> 
int Contrast=75;
//LiquidCrystal(rs, enable, d4, d5, d6, d7)
LiquidCrystal lcd(A4, A5, A0, A1, A2, A3); 
int Contrast_pin=5; 
int Buzzer=4;
int Sw1=2;    //switch 1
int Sw2=3;    //switch 2 


#include <AltSoftSerial.h>

// AltSoftSerial always uses these pins:
//
// Board          Transmit  Receive   PWM Unusable
// -----          --------  -------   ------------
// Teensy 3.0 & 3.1  21        20         22
// Teensy 2.0         9        10       (none)
// Teensy++ 2.0      25         4       26, 27
// Arduino Uno        9         8         10
// Arduino Leonardo   5        13       (none)
// Arduino Mega      46        48       44, 45
// Wiring-S           5         6          4
// Sanguino          13        14         12

AltSoftSerial altSerial;
#include <SPI.h>
#include <RF24.h>
#include <BTLE.h>

RF24 radio(6, 7); // CE, CSN for nrf
BTLE btle(&radio);

int i;
byte Flag = 0;   // flag for next data frame
byte buf[4] = {0xf4, 0x56, 0x00, 0xb6}; //to clear all mode(not used)
byte ECU_REQ[5] = {0xf4, 0x57, 0x01, 0x00, 0xb4}; //req from ECM
byte ECU_RESP[64] = {0x00}; //response from ECM

///////////////////////////////ECU conrol command
byte Check_engine_light_off_REQ[14] = {0xf4, 0x60, 0x04, 0x81, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x27}; //req from ECM
byte Check_engine_light_on_REQ[14] = {0xf4, 0x60, 0x04, 0x81, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x26}; //req from ECM

byte Clear_Malf_Codes[14]={0xF4,0x60,0x04,0x00,0x00,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x98};

byte Fan_high_off[14]={0xF4,0x60,0x04,0x82,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x26};
byte Fan_high_on[14]={0xF4,0x60,0x04,0x82,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x24};

byte Fan_low_off[14]={0xF4,0x60,0x04,0x84,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x24};
byte Fan_low_on[14]={0xF4,0x60,0x04,0x84,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20};

byte AC_Cutout_Relay_off[14]={0xF4,0x60,0x04,0x88,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20};
byte AC_Cutout_Relay_on[14]={0xF4,0x60,0x04,0x88,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x18};


byte EGR_solenoid_control_off[14]={0xF4,0x60,0x04,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xE8};
byte EGR_solenoid_control_on[14]={0xF4,0x60,0x04,0xC0,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xA8};

byte CCP_solenoid_control_off[14]={0xF4,0x60,0x04,0x90,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x18};
byte CCP_solenoid_control_on[14]={0xF4,0x60,0x04,0x90,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x08};

byte VGIS_solenoid_control_off[14]={0xF4,0x60,0x04,0xA0,0x20,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xE8};
byte VGIS_solenoid_control_on[14]={0xF4,0x60,0x04,0xA0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x08};

byte Fuel_ClosedOpen_Loop_closed[14]={0xF4,0x60,0x04,0x00,0x00,0x01,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0xA6};
byte Fuel_ClosedOpen_Loop_open[14]={0xF4,0x60,0x04,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xA7};


byte IAC_mode_control_closed[14]={0xF4,0x60,0x04,0x00,0x00,0x02,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0xA4};
byte IAC_mode_control_open[14]={0xF4,0x60,0x04,0x00,0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xA6};

byte IAC_motor_reset[14]={0xF4,0x60,0x04,0x00,0x00,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xA0};
byte Block_Learn_Memory_reset[14]={0xF4,0x60,0x04,0x00,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xA4};

byte Set_RPM[14]={0xF4,0x60,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0xF0,0x00,0x00,0xB5};
byte Set_AF[14]={0xF4,0x60,0x04,0x00,0x00,0x00,0x00,0x00,0x00,0x04,0x00,0x92,0x00,0x12};
/////////////////////////////////////////////////////////////
//Simple response for test
byte data[64] = {0xF4, 0x92, 0x01, 0x07, 0x41, 0x00, 0x00, 0x00, 0x55, 0x9A, 0x6E, 0xF8, 0xF7, 0xFF, 0x1E, 0x00, 0x63, 0x00, 0x00, 0x00, 0x3F, 0x9A, 0x79, 0x80, 0x00, 0x09, 0x47, 0x47, 0x01, 0x1E, 0x19, 0x23, 0x23, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x96, 0x65, 0x80, 0x00, 0x00, 0xCE, 0x00, 0x00, 0x00, 0x00, 0x84, 0x00, 0x08, 0x00, 0x01, 0x01, 0x00, 0x20, 0x06, 0x00, 0x00, 0xA0, 0x98, 0xb1};

// the setup function runs once when you press reset or power the board
void SendContrlcommandtoECU(byte REC_Android[14]) {
  /////fRAME 2 1 2 9 16 0 0 A A A 2 0 0
  if (REC_Android[7] == 0x0A && REC_Android[8] == 0x0A && REC_Android[9] == 0x0A)
  {

    switch  (REC_Android[10])
    {
      case 0x00: altSerial.write(Clear_Malf_Codes, 14); break;
      case 0x01: altSerial.write(Check_engine_light_on_REQ, 14);    Serial.println("on on "); break;
      case 0x02: altSerial.write(Check_engine_light_off_REQ, 14);    Serial.println("off off "); break;
      case 0x03: altSerial.write(Fan_high_on, 14); break;
      case 0x04: altSerial.write(Fan_high_off, 14); break;
      case 0x05: altSerial.write(Fan_low_on, 14); break;
      case 0x06: altSerial.write(Fan_low_off, 14); break;
      case 0x07: altSerial.write(AC_Cutout_Relay_on, 14); break;
      case 0x08: altSerial.write(AC_Cutout_Relay_off, 14); break;
      case 0x09: altSerial.write(EGR_solenoid_control_on, 14); break;
      case 0x0A: altSerial.write(EGR_solenoid_control_off, 14); break;  
      case 0x0B: altSerial.write(CCP_solenoid_control_on, 14); break;
      case 0x0C: altSerial.write(CCP_solenoid_control_off, 14); break;  
      case 0x0D: altSerial.write(VGIS_solenoid_control_on, 14); break;
      case 0x0E: altSerial.write(VGIS_solenoid_control_off, 14); break;
      case 0x0f: altSerial.write(Fuel_ClosedOpen_Loop_open, 14); break;
      case 0x10: altSerial.write(Fuel_ClosedOpen_Loop_closed, 14); break; 
      case 0x11: altSerial.write(IAC_mode_control_open, 14); break;
      case 0x12: altSerial.write(IAC_mode_control_closed, 14); break; 

       case 0x13: altSerial.write(IAC_motor_reset, 14); break; 
       case 0x14: altSerial.write(Block_Learn_Memory_reset, 14); break; 

       case 0x15: 
       Set_RPM[10]=REC_Android[11];    //
       Set_RPM[13]=REC_Android[12];   //Cheack SUM (512-(0xF4+0x60+0x04+0x00+0x00+0x00+0x00+0x00+0x00+0x03+RPM+0x00+0x00));
       //Serial.print(Set_RPM[10],HEX);
       altSerial.write(Set_RPM, 14); break; 


        case 0x16: 
       Set_AF[11]=REC_Android[11];    //
       Set_AF[13]=REC_Android[12];   //Cheack SUM (512-(0xF4+0x60+0x04+0x00+0x00+0x00+0x00+0x00+0x00+0x03+RPM+0x00+0x00));
       //Serial.print(Set_RPM[10],HEX);
       altSerial.write(Set_AF, 14); break; 



    }
    


  }
}

///////////////////////////LCD Function
int LCD_flag1=0,LCD_flag2=0, LCD_flag3=0;
void LCD_Display()
{
if (digitalRead(Sw1)&&digitalRead(Sw2))
{
  if(LCD_flag1==0){ LCD_flag1=1; LCD_flag2=0; LCD_flag3=0; lcd.clear(); digitalWrite(Buzzer, HIGH);}
  
 
 char outstr1[3]; dtostrf(ECU_RESP[14 + 2]*4.34,3, 0, outstr1); lcd.setCursor(0, 1); lcd.print("O2="); lcd.print(outstr1);// lcd.print("V") ;//_o2voltage
 char outstr2[4]; dtostrf(ECU_RESP[20 + 2]/10.0,4, 1, outstr2); lcd.setCursor(0, 0); lcd.print("Bat="); lcd.print(outstr2);// lcd.print("V") ;//_BATTERY
 char outstr3[3]; dtostrf(ECU_RESP[39 + 2],3, 0, outstr3); lcd.setCursor(9, 0); lcd.print("A/F="); lcd.print(outstr3);// lcd.print("V") ;//_AIR_FUEL_RATIO
 char outstr4[2]; dtostrf(ECU_RESP[7 + 2]* .75 - 40,2, 0, outstr1); lcd.setCursor(8, 1); lcd.print("TMP="); lcd.print(outstr1);  lcd.print("c") ;//ECU_RESP[7 + 2]; //_cooldeg

}

if (!digitalRead(Sw1)&&digitalRead(Sw2))
{
  if(LCD_flag2==0){ LCD_flag1=0; LCD_flag2=1; LCD_flag3=0; lcd.clear(); digitalWrite(Buzzer, HIGH);} 

   char outstr1[3]; dtostrf(5*ECU_RESP[12 + 2]/ 255.0,3, 2, outstr1); lcd.setCursor(0, 0); lcd.print("TPS="); lcd.print(outstr1);// lcd.print("V") ;// ECU_RESP[12 + 2]; //_TPS
   char outstr11[3]; dtostrf(ECU_RESP[17 + 2]* 12.82051 - 128.205,4, 0, outstr11);  lcd.print(" AC="); lcd.print(outstr11); lcd.print("kPa") ;//ECU_RESP[9 + 2]; //_admap

   char outstr2[2]; dtostrf(ECU_RESP[8 + 2]* .75 - 40,2, 0, outstr2); lcd.setCursor(0, 1); lcd.print("MAT="); lcd.print(outstr2); lcd.print("c") ;////ECU_RESP[8 + 2]; //_matdeg
   char outstr3[3]; dtostrf((ECU_RESP[9 + 2]+ 28.06) / 2.71,3, 0, outstr3);  lcd.print(" MAP="); lcd.print(outstr3); lcd.print("kPa") ;//ECU_RESP[9 + 2]; //_admap

    
}

if (digitalRead(Sw1)&&!digitalRead(Sw2))
{
  if(LCD_flag3==0){ LCD_flag1=0; LCD_flag2=0; LCD_flag3=1; lcd.clear(); digitalWrite(Buzzer, HIGH);}

   char outstr1[4]; dtostrf(ECU_RESP[13 + 2]* 25,4, 0, outstr1);  lcd.setCursor(0, 0); lcd.print("RPM="); lcd.print(outstr1); //ECU_RESP[9 + 2]; //_admap
   char outstr2[3]; dtostrf(ECU_RESP[15 + 2]*1.609,3, 0, outstr2);  lcd.print(" SPD="); lcd.print(outstr2); //ECU_RESP[9 + 2]; //_admap

   char outstr3[2]; dtostrf(ECU_RESP[24 + 2],2, 0, outstr3);  lcd.setCursor(0, 1); lcd.print("IAC="); lcd.print(outstr3); //ECU_RESP[9 + 2]; //_admap
   char outstr4[4]; dtostrf((ECU_RESP[37 + 2]*256+ECU_RESP[38 + 2])/ 131.07,2, 1, outstr4);  lcd.print(" BPW="); lcd.print(outstr4); //ECU_RESP[9 + 2]; //_admap



}

}


void setup() {
///////////////LCD+BUZZER+Swithch
//lcd.createChar(0, Heart);
pinMode(Buzzer,OUTPUT);  ///buzzer Hig:on  Low:off
pinMode(Sw1, INPUT_PULLUP);    // sets the digital pin 7 as input
pinMode(Sw2, INPUT_PULLUP);    // sets the digital pin 7 as input
digitalWrite(Buzzer, LOW);
analogWrite(Contrast_pin,Contrast); ///Set LCD contract
lcd.begin(16, 2);
lcd.setCursor(0, 0);
lcd.print("Line 1");
lcd.setCursor(0, 1);
lcd.print("Line 2");
/////////////////////////////////////
  
  //Serial.begin(8192);
  Serial.begin(9600);//test
  while (!Serial) ; // wait for Arduino Serial Monitor to open
  altSerial.begin(8192);
  i = 0; /// counter for recived Data
  btle.begin("");    // nrf24l01
}

// the loop function runs over and over again forever
void loop() {

      //if(digitalRead(Sw2)==LOW)
       //digitalWrite(Buzzer, HIGH);
       //else
              //digitalWrite(Buzzer, LOW);

    
  btle.begin("");
  Flag = 0;
  byte c;
  i = 0;

  byte REC_Android[14]; // Recived Data from android
  memset(REC_Android, 0, sizeof(REC_Android));  // Recived Data from android
  memset(ECU_RESP, 0, sizeof(ECU_RESP));  // ECU_REsp=0
 
  if (Serial.available()) {          ////For connection to pc
    delay(50);  ///untile recive all data
    while (Serial.available())
      altSerial.write(Serial.read());
  }
  else if (btle.listen()) {        ///recive control command From Ardunio

    for (uint8_t ii = 0; ii < (btle.buffer.pl_size) - 6; ii++) {
      REC_Android[ii] = btle.buffer.payload[ii];
      //Serial.print(btle.buffer.payload[i],HEX);
      //Serial.print(" ");
    }

    SendContrlcommandtoECU(REC_Android);
    delay(100);
    altSerial.begin(8192);


  } else
  {
   
     //altSerial.begin(8192);
     delay(8);
    altSerial.write(ECU_REQ, 5); // turn the LED on (HIGH is the voltage level)
    delay(8);  //wait unti dats sent
    altSerial.flushInput();           //clear rx buffer

    delay(350);  ///for test
  }

  while (altSerial.available() )  //read the ECU response
  {
    c = altSerial.read();
    //Serial.write(c);

    ECU_RESP[i] = c;

    i++;
  }

  //i=64; //For test remove
  //Serial.println(i);
  // delay(1500);  ///for test

  if (i == 64) ///send it android Phone (not 64 becuse ardunino rx buffer is 63 byte)
  {
    digitalWrite(Buzzer, LOW);
    i = 0;

    for (int c = 1; c <= 40; c++)
    {
      byte buf1[16] = {0};

      Flag += 1;
      if (Flag == 5)
        Flag = 1;


      if (Flag == 1)
      {

        buf1[0] = ECU_RESP[1 + 2]; //_promid
        buf1[1] = ECU_RESP[2 + 2]; //_promid
        buf1[2] = ECU_RESP[3 + 2]; //_malffw1
        buf1[3] = ECU_RESP[4 + 2]; //_malffw2
        buf1[4] = ECU_RESP[5 + 2]; //_malffw3
        buf1[5] = ECU_RESP[6 + 2]; //_malffw4
        buf1[6] = ECU_RESP[7 + 2]; //_cooldeg
        buf1[7] = ECU_RESP[8 + 2]; //_matdeg
        buf1[8] = ECU_RESP[9 + 2]; //_admap
        buf1[9] = ECU_RESP[10 + 2]; //_BARO
        buf1[10] = ECU_RESP[11 + 2]; //_VACUUM+_MAP
        buf1[11] = ECU_RESP[12 + 2]; //_TPS
        buf1[12] = ECU_RESP[13 + 2]; ///_RPM
        buf1[13] = ECU_RESP[14 + 2]; //_o2voltage
        buf1[14] = ECU_RESP[15 + 2]; //_SPEED
        buf1[15] = ECU_RESP[16 + 2]; //unknown
      }

      if (Flag == 2)
      {
        Flag = 2;
        buf1[0] = ECU_RESP[17 + 2]; //_AC_PRESSURE
        buf1[1] = ECU_RESP[18 + 2]; //unknown
        buf1[2] = ECU_RESP[19 + 2]; //unknown
        buf1[3] = ECU_RESP[20 + 2]; //_BATTERY
        buf1[4] = ECU_RESP[21 + 2]; //unknown
        buf1[5] = ECU_RESP[22 + 2]; //_SPARK_ADVANCE 1
        buf1[6] = ECU_RESP[23 + 2]; //_SPARK_ADVANCE 2
        buf1[7] = ECU_RESP[24 + 2]; //IAC_POS
        buf1[8] = ECU_RESP[25 + 2]; //DESIRED_IAC_POS
        buf1[9] = ECU_RESP[26 + 2]; //unknown
        buf1[10] = ECU_RESP[27 + 2]; //_IDLE_RPM
        buf1[11] = ECU_RESP[28 + 2]; //unknown
        buf1[12] = ECU_RESP[29 + 2]; //unknown
        buf1[13] = ECU_RESP[30 + 2]; //unknown
        buf1[14] = ECU_RESP[31 + 2]; //unknown
        buf1[15] = ECU_RESP[32 + 2]; //unknown

      }

      if (Flag == 3)
      {
        Flag = 3;
        buf1[0] = ECU_RESP[33 + 2]; //unknown
        buf1[1] = ECU_RESP[34 + 2]; //unknown
        buf1[2] = ECU_RESP[35 + 2]; //unknown
        buf1[3] = ECU_RESP[36 + 2]; //unknown
        buf1[4] = ECU_RESP[37 + 2]; //_BPW 1
        buf1[5] = ECU_RESP[38 + 2]; //_BPW 2
        buf1[6] = ECU_RESP[39 + 2]; //_AIR_FUEL_RATIO
        buf1[7] = ECU_RESP[40 + 2]; //_BLM
        buf1[8] = ECU_RESP[41 + 2]; //_BLM_CELL
        buf1[9] = ECU_RESP[42 + 2]; //_INT
        buf1[10] = ECU_RESP[43 + 2]; //unknown
        buf1[11] = ECU_RESP[44 + 2]; //_APW
        buf1[12] = ECU_RESP[45 + 2]; //unknown
        buf1[13] = ECU_RESP[46 + 2]; //unknown
        buf1[14] = ECU_RESP[47 + 2]; //unknown
        buf1[15] = ECU_RESP[48 + 2]; //_MODE_WORDS 1/ _stateByte1

      }

      if (Flag == 4)
      {
        Flag = 4;
        buf1[0] = ECU_RESP[49 + 2]; //_MODE_WORDS 2/ _stateByte2
        buf1[1] = ECU_RESP[50 + 2]; //_MODE_WORDS 3/unknown
        buf1[2] = ECU_RESP[51 + 2]; //_MODE_WORDS 4/ _stateByte3
        buf1[3] = ECU_RESP[52 + 2]; //_FUELMODE_WORDS 1/_stateByte4
        buf1[4] = ECU_RESP[53 + 2]; //_FUELMODE_WORDS 2/_stateByte5
        buf1[5] = ECU_RESP[54 + 2]; //_FUELMODE_WORDS 3/_stateByte6
        buf1[6] = ECU_RESP[55 + 2]; //_FUELMODE_WORDS 4/unknown
        buf1[7] = ECU_RESP[56 + 2]; //_stateByte7
        buf1[8] = ECU_RESP[57 + 2]; //_stateByte8
        buf1[9] = ECU_RESP[58 + 2]; //_stateByte9
        buf1[10] = ECU_RESP[59 + 2]; //_stateByte10
        buf1[11] = ECU_RESP[60 + 2]; //_stateByte11

//
//          for (uint8_t ii = 1; ii <= 60; ii++) {
//     
//      Serial.print(ECU_RESP[ii + 2],HEX);
//      Serial.print(" ");
//    }
//        Serial.println(buf1[9]);
      }


      nrf_service_data buf;
      buf.service_uuid = NRF_TEMPERATURE_SERVICE_UUID;
      buf.value = BTLE::to_nRF_Float(1.5);

      //if (!btle.advertise(0x16, &buf, sizeof(buf))) {
      if (!btle.advertise(Flag, &buf1, sizeof(buf1))) {


      }
      btle.hopChannel();




    }


          LCD_Display();


  }
}
