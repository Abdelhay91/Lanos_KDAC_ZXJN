# Lanos_KDAC_ZXJN

------summary
Components:
1- Ardunio uno
2- nrf24l01
3- Android Phone API +23

Connection as in https://circuitdigest.com/fullimage?i=circuitdiagram_mic/Circuit-Diagram-for-Interfacing-nRF24L01-with-Arduino-for-BLE-Communication.png
(without temp sensor)

Output
ALL ALDL Parameters are shown

----------------------------
Lanos 2006
ECM: KDAC with ZXJN
Protocol: ALDL 5v 8192
Circuit







Communication Method

Arduino Send: F4 57 01 00 B4 (all hex)
ECM response: 64 Byte 
1.	Header: 3 byte (F4 92  01)
2.	Payload: 60 Byte
3.	CRC: 1 Byte



Payload:
Source: "https://lanos.com.ua/forum/topic/11399-programma-dlya-diagnostiki-lanos-156/"

Frameware Virsion
1- _promid 1=07
2- _promid 1=41
------------------------------------------------
fault codes (Each code is encoded with one bit)
3- _malffw1=01
4- _malffw2=07
5- _malffw3=41
6- _malffw4=00 
--------------------------------------------
COOLANT TEMPERATURE = N * .75 - 40 [degrees C]
7-_cooldeg=00
-----------------------------------------------------
Mat (manifold temperature)= N * .75 - 40 [degrees C]
8- _matdeg=00 
-----------------------------------------------------
ADMAP       A/D RESULT FROM MANIFOLD PRES. SENSOR INPUT
                      VOLTS = 5N/255
                      (kpa = (N + 28.06)/2.71)
9- _admap=00
-------------------------------------------------------
10- not used  IDX_BARO
-------------------------------------------------------
11-not used  IDX_VACUUM + IDX_MAP
-------------------------------------------------------
12- TPS voltage ADTHROTT    A/D RESULT FROM TPS INPUT
                      VOLTS = 5 *N/255
_adthrot
-------------------------------------------------------
13- NTRPMX      RPM VARIABLE SCALED 25 RPM / BIT
                    
RPM = N * 25 [rpm]
Engine speed
------------------------------------------------------
14-o2 Sensor Voltage
V=N/255
*this.data[idx] * 4.43D
---------------------------
15-Speed in Miles per hour

--------------------------
16- not used 
-------------------------------------------------------
17-not used IDX_AC_PRESSURE

return new Float(this.data[idx] * 12.82051D - 128.205D);
-------------------------------------------------------
18- not used
-------------------------------------------------------
19-not used
-------------------------------------------------------
20-  BATTERY VOLTS = N * .1 [Volt] On
     ADBAT       TRANSMISSION IGNITION VOLTAGE VARIABLE
                      VOLTS = N/10
-----------------------------------------------------
21- not used
------------------------------------------------------
*22       SAP           SPARK ADVANCE RELATIVE TO TDC  (MSB)
*23       SAP+1         SPARK ADVANCE RELATIVE TO TDC  (LSB)
              double byte value in 2's complement representation
              If Bit 7 of MSB = 0  then result is positive
                 value = ([N33]*256 + [N34])
              If Bit 7 of MSB = 1  then result is negative
                 value = 65536 - ([N33]*256 + [N34])
                        DEGREES = VALUE * 90/256
Two bytes for the ignition timing in degrees to TDC.
UOZ = (256 * MSB + LSB)) * 90/256
If the seventh bit in MSB is equal to one, then this corresponds to a negative lead angle (lag):
UOZ = -1 * (65536 - 256 * MSB + LSB) * 90/256
------------------------------
*24-  iDLE AIR CONTROL DESIRED MOTOR POSIRTION
DESIRED IAC POSITION
N = IAC STEPS
Desired position of idle speed controller.
-----------------
25- IAC motor's current position
                        N = IAC STEPS
 
-------------------
26- DESSPD        DESIRED ENGINE SPEED
                        RPM = N*12.5** 0.5 
-------------------
27-36 NOT USED    IDX_IDLE_RPM
*this.data[idx] * 12.5
----------------------
  dispbpw Base Pulse Width", "Injection Base Pulse Width
37- BPW	Base Pulse Width MSB
38-	BPW+1	Base Pulse Width LSB
32 BPW N = MSEC * 65.536
33 BPW+1

(256 * MSB + LSB) / 65.536 [ms]
*(256 * MSB + LSB) / 131.07
------------
39-AIRFUEL       AIR FUEL RATIO
                        A/F RATIO = N/10
AFR = N * .1
--------------------------------------------
*40-       BLM           BLOCK LEARN MULTIPLIER
*41-       BLMCELL       BLOCK LEARN CELL (0 or 1 ONLY)
---------------------------------------------
*42-INT           CLOSED LOOP INTEGRATOR
                        N = COUNTS
---------------
44- IDX_APW
---------
48- mode words
--------
52 fule mode word
---
53 closed loop word
--------
58-o2 SENSOR READY
n=0x01 TRUE
ELSE FALSE
--------------------





#define MALF1_EST          0x01 // Electronic Spark Timing. Firing point control
#define MALF1_HIMAP        0x02 // High manifold pressure
#define MALF1_EGR          0x04 // Exhaust Gas Recirculation Circuit
#define MALF1_SPEED        0x08 // No signal from speed sensor
#define MALF1_HIMAT        0x10 // High manifold air temperature
#define MALF1_HITPS        0x20 // High signal from Trotle Position Sensor
#define MALF1_LOCOLTEMP    0x40 // Low collant temperature
#define MALF1_O2           0x80 // Oxygen sensor circuit

#define MALF2_ESTASHORT    0x01 // Electronic Spark Timing A shorted at BAT+
#define MALF2_ESTASHORTGND 0x02 // Electronic Spark Timing A shorted at ground
#define MALF2_IMMOB        0x04 // Immobilizer no connection
#define MALF2_OCTANE       0x08 // Octane corrector
#define MALF2_KNOCK        0x10 // Knock sensor
#define MALF2_ECU1         0x20 // ECU Error
#define MALF2_O2REACH      0x40 // O2 Sensor reach
#define MALF2_O2LEAN       0x80 // O2 Sensor lean

#define MALF3_LOTPS        0x01 // Low signal from Trotle Position Sensor
#define MALF3_HICOLTEMP    0x02 // High collant temperature
#define MALF3_IAC          0x04 // Idle Air Control
#define MALF3_KPS          0x08 // Crank position sensor
#define MALF3_ECU2         0x10 // ECU Error
#define MALF3_INJECTOR     0x20 // Injector
#define MALF3_ESTBSHORT    0x40 // Electronic Spark Timing A shorted at BAT+
#define MALF3_ESTBSHORTGND 0x80 // Electronic Spark Timing A shorted at ground

#define MALF4_LOMAP        0x02 // Low manifold pressure
#define MALF4_LOMAT        0x10 // Low manifold air temperature
