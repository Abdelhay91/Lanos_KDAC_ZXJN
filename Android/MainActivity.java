package com.example.lanosaldl2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private boolean Record_Flag = false;


    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner bluetoothLeScanner;


    byte[] recived_bytes;
    TextView rawbytes;
    FileOutputStream fOut;
    BluetoothAdapter bluetoothAdapter;

    @Override
    public void onDestroy() {
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bluetoothLeScanner.stopScan(leScanCallback);
        super.onDestroy();
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bluetoothLeScanner.stopScan(leScanCallback);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rawbytes = (TextView)findViewById(R.id.RawBytes);

       // rawbytes.setText(temp);*/
        mHandler = new Handler();

// you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        // Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }



        bluetoothLeScanner =
                 mBluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();


        ////Scan Button
        final Button Scan = findViewById(R.id.Scan);
        Scan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                mScanning = true;
                scanLeDevice(mScanning);


            }
        });

        //////Record
        final Button Record = findViewById(R.id.Record);
        Record.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Record_Flag = true;


            }
        });





        //////EarseRecord
        final Button EarseRecord = findViewById(R.id.EarseRecord);
        EarseRecord.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                try {
                    fOut = openFileOutput("Data.txt",Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            }
        });

        //////PlayRecord
        final Button PlayRecord = findViewById(R.id.PlayRecord);
        PlayRecord.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //readfromfile();
                    new Thread(new Readfromfile()).start();
                }

            }
        });

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
           // mHandler.postDelayed(new Runnable() {
               // @Override
               // public void run() {
                    //mScanning = false;
                 //   bluetoothLeScanner.stopScan(leScanCallback);

                   // rawbytes.setText("stop scan");
              //  }
            //}, SCAN_PERIOD);
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                    .build();
            mScanning = true;
            bluetoothLeScanner.startScan(null,settings,leScanCallback);
            rawbytes.setText("start scan");
        } else {
            //mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }

    }


    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice btDevice = result.getDevice();
                    ScanRecord scanrecord = result.getScanRecord();
                   byte[] recived_byte = scanrecord.getBytes();

                   //id recived_byte[0]=0x02;
                    // id recived_byte[1]=0x01;
                    // id recived_byte[2]=0x05;
                    // id recived_byte[3]=0x11;

                    if(Byte.toUnsignedInt(recived_byte[2])==5 && Byte.toUnsignedInt(recived_byte[3])==17) {
                        String str = Base64.getEncoder().encodeToString(recived_byte) + "-.";

                        writetofile(str);
                    }

                    String sourceString = "<b>" + "Rawdata:" + "</b> " + bytesToHex(recived_byte);
                    rawbytes.setText(Html.fromHtml(sourceString));

                    //rawbytes.setText("Rawdata:"+bytesToHex(recived_byte));
                    Settexts(recived_byte);


                }
            };

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public  void writetofile(String raw) {

        try {
             fOut = openFileOutput("Data.txt",Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            rawbytes.setText("filed to open file");
        }

        //String str = raw;
        try {
            fOut.write(raw.getBytes());
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    class Readfromfile implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {

            FileInputStream fin = null;
            try {
                fin = openFileInput("Data.txt");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int c = 0;
            String temp="";
            char oldst='0';
            while(true) {
                try {
                    if (!((c = fin.read()) != -1)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                temp = temp + Character.toString((char) c);

                char newst = (char) c;

                if (newst == '.' &&  oldst=='-') {
                    String array1[]= temp.split("-\\.");
                    byte [] recived_byte = Base64.getDecoder().decode(array1[0]);
                    temp="";
                    //rawbytes.setText(bytesToHex(b));


                    String sourceString = "<b>" + "Rawdata:" + "</b> " + bytesToHex(recived_byte);
                    rawbytes.setText(Html.fromHtml(sourceString));

                    Settexts(recived_byte);




                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {


                    oldst = newst;
                }


            }

            try {
                fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            rawbytes.setText("done reading");






        }

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public  void Settexts(byte [] recived_byte)
    {
        if(Byte.toUnsignedInt(recived_byte[2])==5 && Byte.toUnsignedInt(recived_byte[3])==17) {
            ///remove first 4 byte
            ///Battery
            if(Byte.toUnsignedInt(recived_byte[4])==1) {
                String Battery_Voltage = "<b>" + "Battery Voltage:" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[11 + 5]) / 10.0) + " V";
                TextView TBattery_Voltage = (TextView) findViewById(R.id.Battery);
                TBattery_Voltage.setText(Html.fromHtml(Battery_Voltage));

                ///RPM
                String RPM = "<b>" + "RPM:" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[8 + 5]) * 25.0) + " rpm";
                TextView TRPM = (TextView) findViewById(R.id.RPM);
                TRPM.setText(Html.fromHtml(RPM));

                ///O2Voltage
                String O2Voltage = "<b>" + "O2 Voltage Sensor:" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[9 + 5]) * 4.34) + " mV";
                TextView TO2Voltage = (TextView) findViewById(R.id.O2Voltage);
                TO2Voltage.setText(Html.fromHtml(O2Voltage));

                ///O2ready
                String O2enable;
                byte o2 = (byte) (recived_byte[15 + 5] & 0x01);
                if (o2 == 01)
                    O2enable = "<b>" + "O2 Ready:" + "</b> " + "True";
                else
                    O2enable = "<b>" + "O2 Ready:" + "</b> " + "False";
                TextView TO2enable = (TextView) findViewById(R.id.O2enable);
                TO2enable.setText(Html.fromHtml(O2enable));

                ///Tempruture
                String Temperature = "<b>" + "Engine Temperature:" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[4 + 5]) * .75 - 40) + " C";
                TextView TTemperature = (TextView) findViewById(R.id.Temperature);
                TTemperature.setText(Html.fromHtml(Temperature));

                ///TPSVoltage
                String TPSVoltage = "<b>" + "TPS Voltage:" + "</b> " + String.valueOf(5 * Byte.toUnsignedInt(recived_byte[7 + 5]) / 255.0) + " V";
                TextView TTPSVoltage = (TextView) findViewById(R.id.TPSVoltage);
                TTPSVoltage.setText(Html.fromHtml(TPSVoltage));

                ///Speed
                String Speed = "<b>" + "Speed:" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[10 + 5]) * 1.609) + " Km/h";
                TextView TSpeed = (TextView) findViewById(R.id.Speed);
                TSpeed.setText(Html.fromHtml(Speed));

                ///manifoldtemperature
                String manifoldtemperature = "<b>" + "Manifold Temperature (MAT):" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[5 + 5]) * .75 - 40) + " C";
                TextView Tmanifoldtemperature = (TextView) findViewById(R.id.manifoldtemperature);
                Tmanifoldtemperature.setText(Html.fromHtml(manifoldtemperature));

                ///IACcurrentposition
                String IACcurrentposition = "<b>" + "IAC Current Position (IDLE):" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[12 + 5]));
                TextView TIACcurrentposition = (TextView) findViewById(R.id.IACcurrentposition);
                TIACcurrentposition.setText(Html.fromHtml(IACcurrentposition));

                ///AIRFUELRATIO
                String AIRFUELRATIO = "<b>" + "AIR FUEL RATIO:" + "</b> " + String.valueOf(Byte.toUnsignedInt(recived_byte[14 + 5]) / 10.0);
                TextView TAIRFUELRATIO = (TextView) findViewById(R.id.AIRFUELRATIO);
                TAIRFUELRATIO.setText(Html.fromHtml(AIRFUELRATIO));


                ///MANIFOLDPRES
                String MANIFOLDPRES = "<b>" + "MANIFOLD Pressure (MAP):" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[6 + 5]) + 28.06) / 2.71) + " kPa";
                TextView TMANIFOLDPRES = (TextView) findViewById(R.id.MANIFOLDPRES);
                TMANIFOLDPRES.setText(Html.fromHtml(MANIFOLDPRES));

                ///DESIRED_ENGINE_SPEED
                String DESIRED_ENGINE_SPEED = "<b>" + "DESIRED RPM :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[13 + 5]) * 6.3)) + " rpm";
                TextView TDESIRED_ENGINE_SPEED = (TextView) findViewById(R.id.DESIRED_ENGINE_SPEED);
                TDESIRED_ENGINE_SPEED.setText(Html.fromHtml(DESIRED_ENGINE_SPEED));

                ///////////////////Error Codes
                String ERROR = "<b>" + "Errors :" + "</b> ";
                TextView TERROR = (TextView) findViewById(R.id.ERROR);
                if ((recived_byte[0 + 5] & 0x01) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Electronic Spark Timing. Firing point control" + "</p>";
                if ((recived_byte[0 + 5] & 0x02) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "High manifold pressure" + "</p>";
                if ((recived_byte[0 + 5] & 0x04) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Exhaust Gas Recirculation Circuit" + "</p>";
                if ((recived_byte[0 + 5] & 0x08) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "No signal from speed sensor " + "</p>";
                if ((recived_byte[0 + 5] & 0x10) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "High manifold air temperature " + "</p>";
                if ((recived_byte[0 + 5] & 0x20) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "High signal from Trotle Position Sensor " + "</p>";
                if ((recived_byte[0 + 5] & 0x40) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Low collant temperature " + "</p>";
                if ((recived_byte[0 + 5] & 0x80) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Oxygen sensor circuit " + "</p>";

                if ((recived_byte[1 + 5] & 0x01) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Electronic Spark Timing A shorted at BAT+" + "</p>";
                if ((recived_byte[1 + 5] & 0x02) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Electronic Spark Timing A shorted at ground " + "</p>";
                if ((recived_byte[1 + 5] & 0x04) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Immobilizer no connection " + "</p>";
                if ((recived_byte[1 + 5] & 0x08) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Octane corrector " + "</p>";
                if ((recived_byte[1 + 5] & 0x10) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Knock sensor " + "</p>";
                if ((recived_byte[1 + 5] & 0x20) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "ECU Error " + "</p>";
                if ((recived_byte[1 + 5] & 0x40) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "O2 Sensor reach " + "</p>";
                if ((recived_byte[1 + 5] & 0x80) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "O2 Sensor lean " + "</p>";

                if ((recived_byte[2 + 5] & 0x01) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Low signal from Trotle Position Sensor" + "</p>";
                if ((recived_byte[2 + 5] & 0x02) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "High collant temperature " + "</p>";
                if ((recived_byte[2 + 5] & 0x04) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Idle Air Control " + "</p>";
                if ((recived_byte[2 + 5] & 0x08) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Crank position sensor " + "</p>";
                if ((recived_byte[2 + 5] & 0x10) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "ECU Error " + "</p>";
                if ((recived_byte[2 + 5] & 0x20) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Injector " + "</p>";
                if ((recived_byte[2 + 5] & 0x40) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Electronic Spark Timing A shorted at BAT+ " + "</p>";
                if ((recived_byte[2 + 5] & 0x80) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Electronic Spark Timing A shorted at ground " + "</p>";

                if ((recived_byte[3 + 5] & 0x02) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Low manifold pressure" + "</p>";
                if ((recived_byte[3 + 5] & 0x10) != 0)
                    ERROR = ERROR + "<p style=\"color:red\">" + "Low manifold air temperature\n " + "</p>";


                TERROR.setText(Html.fromHtml(ERROR));
            } else
            {
                ///Injection Base Pulse Width
                String BPW = "<b>" + "Injection Base Pulse Width :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[3 + 5]) * 256+Byte.toUnsignedInt(recived_byte[4 + 5]))/ 131.07) + " ms";
                TextView TBPW = (TextView) findViewById(R.id.BPW);
                TBPW.setText(Html.fromHtml(BPW));

                //IAC Desired
                String IDLEIAC = "<b>" + "IAC Desired (Idle) :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[2 + 5]) ));
                TextView TIDLEIAC= (TextView) findViewById(R.id.IDLEIAC);
                TIDLEIAC.setText(Html.fromHtml(IDLEIAC));

                //octan
                String Octan="<b>" + "Octan :" + "</b> " + "";;
                if((recived_byte[8 + 5]&0xe0)==0x00)
                    Octan = "<b>" + "Octan :" + "</b> " + "83";
                if((recived_byte[8 + 5]&0xe0)==0x20)
                    Octan = "<b>" + "Octan :" + "</b> " + "87";
                if((recived_byte[8 + 5]&0xe0)==0xa0)
                    Octan = "<b>" + "Octan :" + "</b> " + "91";
                if((recived_byte[8 + 5]&0xe0)==0x80)
                    Octan = "<b>" + "Octan :" + "</b> " + "95";
                TextView TOctan= (TextView) findViewById(R.id.selectedoctan);
                TOctan.setText(Html.fromHtml(Octan));

                //SAP
                String SAP = "<b>" + "Sparc Advance relative to Top Dead Center :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[0 + 5]) * 256+Byte.toUnsignedInt(recived_byte[1 + 5]))*90/255) + " ";
                TextView TSAP = (TextView) findViewById(R.id.SAP);
                TSAP.setText(Html.fromHtml(SAP));

                //BLM
                String BLM = "<b>" + "Block Learn Multiplier :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[5 + 5]) )) + " ";
                TextView TBLM = (TextView) findViewById(R.id.BLM);
                TBLM.setText(Html.fromHtml(BLM));


                //BLMCELL
                String BLMCELL = "<b>" + "Block Learn Multiplier (BLMCELL) :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[6 + 5]) )) + " ";
                TextView TBLMCELL = (TextView) findViewById(R.id.BLMCELL);
                TBLMCELL.setText(Html.fromHtml(BLMCELL));

                //INT
                String INT = "<b>" + "CLOSED LOOP INTEGRATOR :" + "</b> " + String.valueOf((Byte.toUnsignedInt(recived_byte[7 + 5]) )) + " ";
                TextView TINT = (TextView) findViewById(R.id.INT);
                TINT.setText(Html.fromHtml(INT));

                ///FANREQ
                String FANREQ;
                byte FANREQ_ = (byte) (recived_byte[8 + 5] & 0x04);
                if (FANREQ_ == 01)
                    FANREQ = "<b>" + "FAN Request :" + "</b> " + "True";
                else
                    FANREQ = "<b>" + "FAN Request:" + "</b> " + "False";
                TextView TFANREQ = (TextView) findViewById(R.id.FANREQ);
                TFANREQ.setText(Html.fromHtml(FANREQ));


                ///FANREQ
                String dtCoolantSw;
                byte dtCoolantSw_ = (byte) (recived_byte[9 + 5] & 0x04);
                if (dtCoolantSw_ == 01)
                    dtCoolantSw = "<b>" + "Coolant temperature switch:" + "</b> " + "True";
                else
                    dtCoolantSw = "<b>" + "Coolant temperature switch:" + "</b> " + "False";
                TextView TdtCoolantSw = (TextView) findViewById(R.id.dtCoolantSw);
                TdtCoolantSw.setText(Html.fromHtml(dtCoolantSw));


                ///HIGHFAN
                String HIGHFAN;
                byte HIGHFAN_ = (byte) (recived_byte[9 + 5] & 0x08);
                if (HIGHFAN_ == 01)
                    HIGHFAN = "<b>" + "Hign fan is on:" + "</b> " + "True";
                else
                    HIGHFAN = "<b>" + "Hign fan is on:" + "</b> " + "False";
                TextView THIGHFAN = (TextView) findViewById(R.id.HIGHFAN);
                THIGHFAN.setText(Html.fromHtml(HIGHFAN));

                ///LOWFAN
                String LOWFAN;
                byte LOWFAN_ = (byte) (recived_byte[9 + 5] & 0x20);
                if (LOWFAN_ == 01)
                    LOWFAN = "<b>" + "Low fan is on:" + "</b> " + "True";
                else
                    LOWFAN = "<b>" + "Low fan is on:" + "</b> " + "False";
                TextView TLOWFAN = (TextView) findViewById(R.id.LOWFAN);
                TLOWFAN.setText(Html.fromHtml(LOWFAN));

                ///FUELCUTOFF
                String FUELCUTOFF;
                byte FUELCUTOFF_ = (byte) (recived_byte[10 + 5] & 0x10);
                if (FUELCUTOFF_ == 01)
                    FUELCUTOFF = "<b>" + "Safety fuel cut off:" + "</b> " + "True";
                else
                    FUELCUTOFF = "<b>" + "Safety fuel cut off:" + "</b> " + "False";
                TextView TFUELCUTOFF = (TextView) findViewById(R.id.FUELCUTOFF);
                TFUELCUTOFF.setText(Html.fromHtml(FUELCUTOFF));

                ///FUELPUMP
                String FUELPUMP;
                byte FUELPUMP_ = (byte) (recived_byte[10 + 5] & 0x04);
                if (FUELPUMP_ == 01)
                    FUELPUMP = "<b>" + "Fuel pump enabled:" + "</b> " + "True";
                else
                    FUELPUMP = "<b>" + "Fuel pump enabled:" + "</b> " + "False";
                TextView TFUELPUMP = (TextView) findViewById(R.id.FUELPUMP);
                TFUELPUMP.setText(Html.fromHtml(FUELPUMP));


                ///FLDSRVC
                String FLDSRVC;
                byte FLDSRVC_ = (byte) (recived_byte[11 + 5] & 0x04);
                if (FLDSRVC_ == 01)
                    FLDSRVC = "<b>" + "Field service transition:" + "</b> " + "True";
                else
                    FLDSRVC = "<b>" + "Field service transition:" + "</b> " + "False";
                TextView TFLDSRVC = (TextView) findViewById(R.id.FLDSRVC);
                TFLDSRVC.setText(Html.fromHtml(FLDSRVC));


                ///CHARPURGE
                String CHARPURGE;
                byte CHARPURGE_ = (byte) (recived_byte[11 + 5] & 0x80);
                if (CHARPURGE_ == 01)
                    CHARPURGE = "<b>" + "Charcoal canister purge duty cycle:" + "</b> " + "True";
                else
                    CHARPURGE = "<b>" + "Charcoal canister purge duty cycle:" + "</b> " + "False";
                TextView TCHARPURGE = (TextView) findViewById(R.id.CHARPURGE);
                TCHARPURGE.setText(Html.fromHtml(CHARPURGE));


                ///VGIS
                String VGIS;
                byte VGIS_ = (byte) (recived_byte[11 + 5] & 0x40);
                if (VGIS_ == 01)
                    VGIS = "<b>" + "Variable Geometry Induction System:" + "</b> " + "True";
                else
                    VGIS = "<b>" + "Variable Geometry Induction System:" + "</b> " + "False";
                TextView TVGIS = (TextView) findViewById(R.id.VGIS);
                TVGIS.setText(Html.fromHtml(VGIS));

                ///TPSCLOSED
                String TPSCLOSED;
                byte TPSCLOSED_ = (byte) (recived_byte[12 + 5] & 0x01);
                if (TPSCLOSED_ == 01)
                    TPSCLOSED = "<b>" + "TPS Closed:" + "</b> " + "True";
                else
                    TPSCLOSED = "<b>" + "TPS Closed:" + "</b> " + "False";
                TextView TTPSCLOSED = (TextView) findViewById(R.id.TPSCLOSED);
                TTPSCLOSED.setText(Html.fromHtml(TPSCLOSED));

                ///DECELCUTOFF
                String DECELCUTOFF;
                byte DECELCUTOFF_ = (byte) (recived_byte[12 + 5] & 0x40);
                if (DECELCUTOFF_ == 01)
                    DECELCUTOFF = "<b>" + "Deceleration fuel cut-off:" + "</b> " + "True";
                else
                    DECELCUTOFF = "<b>" + "Deceleration fuel cut-off:" + "</b> " + "False";
                TextView TDECELCUTOFF = (TextView) findViewById(R.id.DECELCUTOFF);
                TDECELCUTOFF.setText(Html.fromHtml(DECELCUTOFF));


                ///CATCONVOVER
                String CATCONVOVER;
                byte CATCONVOVER_ = (byte) (recived_byte[13 + 5] & 0x80);
                if (CATCONVOVER_ == 01)
                    CATCONVOVER = "<b>" + "Catalytic converter overtemp:" + "</b> " + "True";
                else
                    CATCONVOVER = "<b>" + "Catalytic converter overtemp:" + "</b> " + "False";
                TextView TCATCONVOVER = (TextView) findViewById(R.id.CATCONVOVER);
                TCATCONVOVER.setText(Html.fromHtml(CATCONVOVER));

                ///IACRUN
                String IACRUN;
                byte IACRUN_ = (byte) (recived_byte[14 + 5] & 0x08);
                if (IACRUN_ == 01)
                    IACRUN = "<b>" + "IAC is run (Idle):" + "</b> " + "True";
                else
                    IACRUN = "<b>" + "IAC is run (Idle):" + "</b> " + "False";
                TextView TIACRUN = (TextView) findViewById(R.id.IACRUN);
                TIACRUN.setText(Html.fromHtml(IACRUN));


                ///IDLERUN
                String IDLERUN;
                byte IDLERUN_ = (byte) (recived_byte[15 + 5] & 0x04);
                if (IDLERUN_ == 01)
                    IDLERUN = "<b>" + "Idle Run:" + "</b> " + "True";
                else
                    IDLERUN = "<b>" + "Idle Run:" + "</b> " + "False";
                TextView TIDLERUN = (TextView) findViewById(R.id.IDLERUN);
                TIDLERUN.setText(Html.fromHtml(IDLERUN));


                ///INTRESET
                String INTRESET;
                byte INTRESET_ = (byte) (recived_byte[15 + 5] & 0x40);
                if (INTRESET_ == 01)
                    INTRESET = "<b>" + "Reset fuel integrator:" + "</b> " + "True";
                else
                    INTRESET = "<b>" + "Reset fuel integrator:" + "</b> " + "False";
                TextView TINTRESET = (TextView) findViewById(R.id.INTRESET);
                TINTRESET.setText(Html.fromHtml(INTRESET));


                ///RPMLOW
                String RPMLOW;
                byte RPMLOW_ = (byte) (recived_byte[15 + 5] & 0x08);
                if (RPMLOW_ == 01)
                    RPMLOW = "<b>" + "RPM is low:" + "</b> " + "True";
                else
                    RPMLOW = "<b>" + "RPM is low:" + "</b> " + "False";
                TextView TRPMLOW = (TextView) findViewById(R.id.RPMLOW);
                TRPMLOW.setText(Html.fromHtml(RPMLOW));

            }
        }

    }

}
