package com.accent_systems.ibkshelloworld;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.*;
import java.util.stream.DoubleStream;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GRAY;
import static java.lang.Math.abs;

public class ScanActivity extends AppCompatActivity {

    //DEFINE VARS
    String TAG = "ScanActivity";

    BluetoothAdapter mBluetoothAdapter;
    BluetoothGatt mBluetoothGatt;
    BluetoothLeScanner scanner;
    ScanSettings scanSettings;
    Button btnStop, btnStart;
    EditText etFileName;
    Boolean doScan;
    //File Writing stuff
    File gpxfile, root1;
    FileWriter writer;
    String fileName;
    Date now = new Date();
    SimpleDateFormat  formatter = new SimpleDateFormat("MM.dd.");
    String uuid, nanoSmartphone, nanoBT;
    //2D-Table for comparing values, arrays with averaged values from measurements
    List<double[]> theTable = new ArrayList<>();
    GridLayout gridMap;
    //arrays for received data for each beacon
    int amountOfMeasurmentsPerBeacon = 10;
    List<Integer> tmp1 = new ArrayList<>();
    List<Integer> tmp2 = new ArrayList<>();
    List<Integer> tmp3 = new ArrayList<>();
    List<Integer> tmp4 = new ArrayList<>();
    //MAC-Adresses of used Beacons
    private static final String macA = "A1";
    private static final String macB = "B2";
    private static final String macC = "C3";
    private static final String macD = "D4";

    private List<String> scannedDeivcesList;
    private ArrayAdapter<String> adapter;

    //DEFINE LAYOUT
    ListView devicesList;

    //THIS METHOD RUNS ON APP LAUNCH
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_scan);

        //Define listview in layout
        devicesList = (ListView) findViewById(R.id.devicesList);
        //Setup list on device click listener
        //setupListClickListener();

        //Inicialize de devices list
        scannedDeivcesList = new ArrayList<>();
        doScan = false;
        btnStop = (Button)findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScanAndSaveFile();
            }
        });
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
        gridMap = (GridLayout) findViewById(R.id.gridMap);
       View view = gridMap.getChildAt(15);
        TextView tv = (TextView) view;
        tv.setBackgroundColor(BLUE);
        etFileName = (EditText) findViewById(R.id.etFileName);
        etFileName.setText("");
        //Initialize Table
        //TODO: Add values here
        //A 1-8
        theTable.add(new double[]{-89.08441558,-83.0474934,-59.9166667,-91.24621212});
        theTable.add(new double[]{-93.07643312,-77.22012579,-69.34795764,-92.37359551});
        theTable.add(new double[]{-93.33766234,-79.17412935,-72.9093702,-91.94315245});
        theTable.add(new double[]{-89.82852807,-79.63456091,-77.94796062,-91.28343949});
        theTable.add(new double[]{-91.59259259,-81.45337159,-77.10932945,-86,62214411});
        theTable.add(new double[]{-93.44060475,-79.17838542,-70.79381443,-92.53978159});
        theTable.add(new double[]{-90.53007519,-70.53780314,-79.75971223,-90.9784689});
        theTable.add(new double[]{-93.41860465,-64.84447301,-79.4057971,-91.62571977});
        //B 1-8
        theTable.add(new double[]{-93.44060475,-79.17838542,-70.79381443,-92.53978159});
        theTable.add(new double[]{-90.14285714,-82.13928571,-79.24574669,-95.08888889});
        theTable.add(new double[]{-90.18377088,-84.65081724,-83.84384858,-89.18608414});
        theTable.add(new double[]{-90.99089253,-76.50229008,-82.78979907,-89.10291595});
        theTable.add(new double[]{-93.10311284,-76.46185286,-83.12654746,-87.15320911});
        theTable.add(new double[]{-91.19815668,-73.74090247,-83.9623494,-86.55784469});
        theTable.add(new double[]{-91.10541311,-73.6260274,-83.69230769,-89.12997347});
        theTable.add(new double[]{-90.85,-74.24383164,-85.91680815,-88.41706161});
        //C 1-8
        theTable.add(new double[]{-91.42669584,-77.96275072,-82.81942337,-95.59722222});
        theTable.add(new double[]{-93.84358974,-78.27871148,-85.17400881,-91.18708241});
        theTable.add(new double[]{-92.20673077,-78.89044944,-78.0227596,-90.71582181});
        theTable.add(new double[]{-93.70038911,-76.24541608,-81.9704579,-89.28399312});
        theTable.add(new double[]{-92.41947566,-77.87839433,-87.29827089,-89.37946429});
        theTable.add(new double[]{-94.01269841,-80.22063666,-87.22560976,-87.97730496});
        theTable.add(new double[]{-91.69978402,-77.7125,-86.20382166,-86.76072607});
        theTable.add(new double[]{-93.18421053,-78.41267788,-87.79514416,-86.6372549});
        //D 1-8
        theTable.add(new double[]{-93.64,-85.59393939,-83.78268877,-91.01807229});
        theTable.add(new double[]{-94.03470032,-80.22063666,-87.22560976,-87.97730496});
        theTable.add(new double[]{-89.15039578,-82.8,-81.16055046,-89.7371134});
        theTable.add(new double[]{-92.44664032,-79.90361446,-82.84615385,-87.43851133});
        theTable.add(new double[]{-91.55172414,-82.43922204,-81.65116279,-85.34621578});
        theTable.add(new double[]{-91.58630137,-81.2435312,-82.87066246,-82.59703704});
        theTable.add(new double[]{-95.45454545,-80.88355167,-82.8011811,-82.58139535});
        theTable.add(new double[]{-95.45454545,-80.88355167,-82.8011811,-82.58139535});
        //E 1-8
        theTable.add(new double[]{-91.44297082,-84.71750433,-86.71223022,-91.13970588});
        theTable.add(new double[]{-91.73501577,-83.44852941,-80.78695652,-92.09026549});
        theTable.add(new double[]{-91.22025316,-82.54769737,-84.80555556,-83.7897351});
        theTable.add(new double[]{-89.62183544,-79.56805556,-85.14979757,-88});
        theTable.add(new double[]{-90.10496183,-82.40974212,-84.41715976,-87.4866562});
        theTable.add(new double[]{-90.94573643,-79.48933144,-79.24964539,-85.90181269});
        theTable.add(new double[]{-93.89310345,-76.80029806,-82.58888889,-83.3516129});
        theTable.add(new double[]{-90.96905537,-82.42547033,-82.69405099,-82.74273256});
        //F 1-8
        theTable.add(new double[]{-90.73026316,-83.36711281,-84.20408163,-91.8375});
        theTable.add(new double[]{-91.90253411,-85.84520548,-87.35155096,-91.81972265});
        theTable.add(new double[]{-90.26953125,-85.98083067,-86.33552632,-90.41891892});
        theTable.add(new double[]{-93.17307692,-85.06419401,-85.40532959,-88.28429423});
        theTable.add(new double[]{-87.81818182,-82.48756906,-88.86587436,-86.22655123});
        theTable.add(new double[]{-93.93129771,-85.85176471,-84.93294461,-83.23665893});
        theTable.add(new double[]{-93.9351145,-85.84958872,-84.93294461,-83.23665893});
        theTable.add(new double[]{-88.11453744,-84.62465374,-83.38432836,-84.71565934});
        //G 1-8
        theTable.add(new double[]{-89.39705882,-85.86732673,-81.4556962,-91.91762014});
        theTable.add(new double[]{-90.76335878,-82.14710042,-82.23748212,-93.43205575});
        theTable.add(new double[]{-94.60373444,-85.99409158,-83.22543353,-92.41825902});
        theTable.add(new double[]{-88.3234375,-87.65470085,-88.07448494,-85.22857143});
        theTable.add(new double[]{-93.55778894,-85.64739884,-86.81884058,-86.86309524});
        theTable.add(new double[]{-92.77040816,-84.52037618,-86.30821918,-86.73220339});
        theTable.add(new double[]{-90.4205298,-84.48923077,-85.61643836,-81.93510324});
        theTable.add(new double[]{-89.05871212,-84.48307692,-89.89004458,-80.77806789});
        //H 1-8
        theTable.add(new double[]{-86.09700428,-85.85856574,-86.02668539,-87.22574257});
        theTable.add(new double[]{-86.36088154,-84.65620542,-89.35034014,-9.,46995378});
        theTable.add(new double[]{-88.57668712,-83.80763583,-90.90356394,-87.30518519});
        theTable.add(new double[]{-88.89051095,-88.28790199,-86.08384146,-88.56515152});
        theTable.add(new double[]{-88.68794326,-84.31206657,-88.45441595,-86.26359833});
        theTable.add(new double[]{-89.9318542,-85.98521257,-87.84722222,-86.65968586});
        theTable.add(new double[]{-88.83298097,-86.54545455,-89.66736842,-81.73752711});
        theTable.add(new double[]{-89.7512275,-89.77793696,-88.76171875,-85.46507353});
        // I 1-8
        theTable.add(new double[]{-88.82748092,-89.0372093,-88.02050473,-87.18292683});
        theTable.add(new double[]{-89.69749216,-90.36057692,-83.68325792,-83.87645688});
        theTable.add(new double[]{-89.9318542,-85.98521257,-87.84722222,-86.65968586});
        theTable.add(new double[]{-89.98253275,-82.49462366,-83.05790297,-80,63343558});
        theTable.add(new double[]{-89.39495798,-81.90666667,-84.09103261,-84.42949547});
        theTable.add(new double[]{-86.32918396,-86.24209078,-86.45310245,-90.51641791});
        theTable.add(new double[]{-87.23284823,-83.43029491,-93.11820331,-91.31239669});
        theTable.add(new double[]{-89.290625,-85.79456193,-88.42559524,-82.9382716});
        //J 1-8
        theTable.add(new double[]{-88.93191489,-85.21757322,-87.86303387,-91.27591707});
        theTable.add(new double[]{-91.22435897,-91.03154574,-85.07837446,-84.66169896});
        theTable.add(new double[]{-88.69269521,-89.00847458,-87.07051282,-83.97938144});
        theTable.add(new double[]{-93.90243902,-91.34166667,-85.75073746,-80.7146933});
        theTable.add(new double[]{-89.3151184,-85.28035982,-85.18057663,-81.70977011});
        theTable.add(new double[]{-89.31285988,-82.03821656,-89.16952055,-82.90393701});
        theTable.add(new double[]{-89.49222798,-84.06225681,-86.50862069,-86.82463466});
        theTable.add(new double[]{-91.51964286,-87.35343384,-82.04782609,-92.14669421});
        //K 1-8
        theTable.add(new double[]{-84.22151899,-84.73497689,-89.46829268,-80.96764253});
        theTable.add(new double[]{-86.12927192,-86.10795455,-88.43939394,-84.26153846});
        theTable.add(new double[]{-84.69056604,-87.14225941,-88.52730109,-84.90979782});
        theTable.add(new double[]{-82.18723994,-90.73501577,-88.82361309,-85.89189189});
        theTable.add(new double[]{-90.8786828,-92.20980926,-91.72025052,-81,69764012});
        theTable.add(new double[]{-89.97819315,-91.74255319,-88.63242009,-80.44588745});
        theTable.add(new double[]{-84.14596273,-93.22789116,-89.3704918,-77.74772036});
        theTable.add(new double[]{-83.29453015,-89.55729984,-89.54,-83.30312036});
        //L 1-8
        theTable.add(new double[]{-86.40118871,-90.43642612,-93.2408377,-86.54478708});
        theTable.add(new double[]{-85.14389535,-88.73784355,-89.85876623,-80.8220339});
        theTable.add(new double[]{-87.54931507,-85.61985816,-90.5093985,-77.79782904});
        theTable.add(new double[]{-86.14553314,-87.17125382,-86.24457308,-83.22998544});
        theTable.add(new double[]{-86.18392857,-89.4516129,-90.05376344,-85.45183888});
        theTable.add(new double[]{-89.57581227,-89.54212454,-93.81283422,-79.7086743});
        theTable.add(new double[]{-88.49061662,-90,98876404,-92.9031339,-83.44740177});
        theTable.add(new double[]{-89.70881671,-91.64238411,-93.65400844,-81.68735632});
        //M 1-8
        theTable.add(new double[]{-89.70881671,-91.64238411,-93.65400844,-81.68735632});
        theTable.add(new double[]{-85,24362606,-88.41690962,-90.29239766,-88.92105263});
        theTable.add(new double[]{-87.91412742,-90.98773006,-86.96883853,-78.72144847});
        theTable.add(new double[]{-85.85630499,-95.72173913,-89.23631124,-74,8356546});
        theTable.add(new double[]{-83.94025974,-89.79032258,-96.58119658,-79.9697733});
        theTable.add(new double[]{-81.48863636,-88.11976048,-96.46666667,-82.87319885});
        theTable.add(new double[]{-82.34877384,-89.23611111,-90.96261682,-80.912});
        theTable.add(new double[]{-86.93899204,-89.85866667,-87.94755245,-80.5026738});
        //N 1-6
        theTable.add(new double[]{-86.37319317,-93.77707006,-90.54615385,-80.0268886});
        theTable.add(new double[]{-86.22036082,-93.60276074,-89.69179894,-80.46624204});
        theTable.add(new double[]{-84.65206186,-91.50413223,-85.9095675,-74.50678175});
        theTable.add(new double[]{-81.90924956,-90.06754967,-90.93829787,-81,44723618});
        theTable.add(new double[]{-81.26027397,-93.70860927,-92.4142539,-80.29565217});
        theTable.add(new double[]{-87.58727811,-93.55525606,-90.55102041,-86.66960352});
        theTable.add(new double[]{-79.21369863,-89.345,-87.51560178,-83.09264305});
        theTable.add(new double[]{-86.49361702,-93.56071429,-93.30627306,-82.16305916});
//        //O 1-6
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        theTable.add(new double[]{});
//        //P1-8
//        theTable.add(new double[]{-86.68421053,-92.92033898,-90.1686747,-76.33541927});
//        theTable.add(new double[]{-88.88300221,-91.34579439,-84.97864769,-78.00168067});
//        theTable.add(new double[]{-83.21587302,-92.53971963,-89.22586207,-77.32403101});
//        theTable.add(new double[]{-87.63867188,-90.69154229,-91.09810671,-80.21963394});
//        theTable.add(new double[]{-84.40606061,-90.32453416,-90.78978979,-84.49423394});
//        theTable.add(new double[]{-85.14710042,-92.48281016,-90.95601173,-77.61126374});
//        theTable.add(new double[]{-81.09090909,-92.11897106,-89.99634369,-84.74007682});
//        theTable.add(new double[]{-76.37837838,-86.89516129,-93.28764479,-83.67464789});
//        //Q 1-8
//        theTable.add(new double[]{-84.42527174,-91.45720251,-90.62550607,-69.63471503});
//        theTable.add(new double[]{-81.54783748,-89.78719397,-88.29634831,-73.67399741});
//        theTable.add(new double[]{-86.12250333,-90.11764706,-91.17771509,-75.87580026});
//        theTable.add(new double[]{-82.54248366,-91.88940092,-86.66568483,-77.9626556});
//        theTable.add(new double[]{-82.88027211,-88.35515873,-88.75280899,-79.3032345});
//        theTable.add(new double[]{-82.54248366,-91.88940092,-86.66568483,-77.9626556});
//        theTable.add(new double[]{-82.59801136,-86.69845722,-87.8772242,-84.84366197});
//        theTable.add(new double[]{-79.1179941,-93.50331126,-90.29069767,-81.73109244});


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       

        getSupportActionBar().setLogo(R.mipmap.ibks);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        

    }

    private void startScan() {
        if( etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
            //Enable/disable start and stop
            btnStop.setEnabled(true);
            btnStart.setEnabled(false);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, scannedDeivcesList);
            //Set the adapter to the listview
            devicesList.setAdapter(adapter);

            //initialize file writing stuff
            checkPermissions();
            fileName = etFileName.getText().toString()+"-" + formatter.format(now)+".txt";
            Environment.getExternalStorageDirectory();
            try {
                root1 = new File(Environment.getExternalStorageDirectory() + File.separator + "Beacon_Folder", "Data");
                if (!root1.exists())
                    root1.mkdirs();
                gpxfile = new File(root1, fileName);
                writer = new FileWriter(gpxfile, true);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //init Bluetooth adapter
            initBT();
            //Start scan of bluetooth devices
            doScan = true;
            startLeScan(true);

        }
        else Toast.makeText(getApplicationContext(), "Kein Dateiname.", Toast.LENGTH_LONG);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Darf nicht schreiben auf Ext. Storage", Toast.LENGTH_LONG).show();
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Darf nicht lesen auf Ext. Storage", Toast.LENGTH_LONG).show();
        }

    }

    private void stopScanAndSaveFile() {
        //Enable/disable start stop buttons

        btnStop.setEnabled(false);
        etFileName.setText("");
        try {
            doScan = false;
            writer.flush();
            writer.close();
            Toast.makeText(getApplicationContext(), "SAVED", Toast.LENGTH_LONG).show();
            btnStart.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        startLeScan(false);
    }

    private void initBT(){
        final BluetoothManager bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //Create the scan settings
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        //Set scan latency mode. Lower latency, faster device detection/more battery and resources consumption
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //Wrap settings together and save on a settings var (declared globally).
        scanSettings = scanSettingsBuilder.build();
        //Get the BLE scanner from the BT adapter (var declared globally)
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void startLeScan(boolean endis) {
        if (endis) {
            //********************
            //START THE BLE SCAN
            //********************
            //Scanning parameters FILTER / SETTINGS / RESULT CALLBACK. Filter are used to define a particular
            //device to scan for. The Callback is defined above as a method.
            scanner.startScan(null, scanSettings, mScanCallback);
        }else{
            //Stop scan
            scanner.stopScan(mScanCallback);
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);


            //Only listen for iBKS devices
            if(result.getDevice().getName() != null && result.getDevice().getName().contains("iBKS")){


            //Here will be received all the detected BLE devices around. "result" contains the device
            //address and name as a BLEPeripheral, the advertising content as a ScanRecord, the Rx RSSI
            //and the timestamp when received. Type result.get... to see all the available methods you can call.

            //Convert advertising bytes to string for a easier parsing. GetBytes may return a NullPointerException. Treat it right(try/catch).
            String advertisingString = byteArrayToHex(result.getScanRecord().getBytes());
            //Print the advertising String in the LOG with other device info (ADDRESS - RSSI - ADVERTISING - NAME)
            Log.i(TAG, result.getDevice().getAddress() + " - RSSI: " + result.getRssi() + "\t - " + advertisingString + " - " + result.getDevice().getName());

            //Check if scanned device is already in the list by mac address
            //boolean contains = false;
           // for (int i = 0; i < scannedDeivcesList.size(); i++) {
              //  if (scannedDeivcesList.get(i).contains(result.getDevice().getAddress())) {
                   // contains = true;
                    //Replace the device with updated values in that position
                 //   scannedDeivcesList.set(i, result.getRssi() + "  " + result.getDevice().getName() + "\n       (" + result.getDevice().getAddress() + ")");

                    //Write rssi to file!
                    try {
                        //getting Smartphone-Time and BT-TImemmm
//                        nanoSmartphone = String.valueOf(System.nanoTime());
//                        nanoBT = String.valueOf(result.getTimestampNanos());
                        if(tmp1.size() == amountOfMeasurmentsPerBeacon || tmp2.size() == amountOfMeasurmentsPerBeacon || tmp3.size() == amountOfMeasurmentsPerBeacon || tmp4.size() == amountOfMeasurmentsPerBeacon)
                            checkPosition(getAverage(tmp1), getAverage(tmp2), getAverage(tmp3), getAverage(tmp4));
                        else
                        {
                            int rssi = result.getRssi();
                            switch (result.getDevice().getAddress()){
                                case macA:
                                    tmp1.add(rssi);
                                    break;
                                case macB:
                                    tmp2.add(rssi);
                                    break;
                                case macC:
                                    tmp2.add(rssi);
                                    break;
                                case macD:
                                    tmp2.add(rssi);
                                    break;
                                default:
                                    break;
                            }

                        }

                        writer.append(nanoSmartphone + "," + result.getDevice().getAddress() + "," + result.getRssi() + "," + nanoBT + "," + "\n");
                        //}
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                   // break;
              //  }
           // }

//            if (!contains) {
//                //Scanned device not found in the list. NEW => add to list
//                scannedDeivcesList.add(result.getRssi() + "  " + result.getDevice().getAddress() + "\n       (" + result.getDevice().getAddress() + ")");
//                //Write to file
//                try {
//                    //get uuid
//                    if (result.getDevice().getUuids() != null) {
//                        uuid = result.getDevice().getUuids()[0].toString();
//                    } else uuid = "null";
//                    //Smartphone-Time and BT-Time
//                    nanoSmartphone = String.valueOf(System.nanoTime());
//                    nanoBT = String.valueOf(result.getTimestampNanos());
//                    writer.append(nanoSmartphone + "," + result.getDevice().getName() + "," + result.getRssi() + "," + nanoBT + "," + uuid + "\n");
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            //After modify the list, notify the adapter that changes have been made so it updates the UI.
            //UI changes must be done in the main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }

        }
    };
    //this compares the averaged measure-values with the ones in THE TABLE
    private int checkPosition(double a, double b, double c, double d)
    {
        double tmpSubstraction;
        double smallestSubstraction = 500; //this number will change when a smaller substraction is found
        int position = 0; //this will be the calculated pos.

        for (int point = 0; point < theTable.size(); point++) {//addiere die differenzen
                tmpSubstraction = abs(theTable.get(point)[0] - a);
                tmpSubstraction += abs(theTable.get(point)[1] - b);
                tmpSubstraction += abs(theTable.get(point)[2] - c);
                tmpSubstraction += abs(theTable.get(point)[3] - d);
                if (tmpSubstraction < smallestSubstraction) { //wenn die Gesamtdifferenz kleiner ist als die bisherige kleinste
                    smallestSubstraction = tmpSubstraction;
                    position = point;
                }
        }

        return position;
    }

    //this gets the average value of measured measurments
    private double getAverage(List<Integer> tmp) {
        Integer sum = 0;
        if (!tmp.isEmpty()) {

            for (Integer rs : tmp) {
                sum += rs;
            }
            return sum.doubleValue() / tmp.size();
        }
        return sum;
    }

    //Method to convert a byte array to a HEX. string.
    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

//    void setupListClickListener(){
//        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //Stop the scan
//                Log.i(TAG, "SCAN STOPED");
//                scanner.stopScan(mScanCallback);
//
//                //Get the string from the item clicked
//                String fullString = scannedDeivcesList.get(position);
//                //Get only the address from the previous string. Substring from '(' to ')'
//                String address = fullString.substring(fullString.indexOf("(")+1, fullString.indexOf(")"));
//                //Get BLE device with address
//                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//                //******************************
//                //START CONNECTION WITH DEVICE AND DECLARE GATT
//                //******************************
//                Log.i(TAG,"*************************************************");
//                Log.i(TAG, "CONNECTION STARTED TO DEVICE "+address);
//                Log.i(TAG,"*************************************************");
//
//                //ConnectGatt parameters are CONTEXT / AUTOCONNECT to connect the next time it is scanned / GATT CALLBACK to receive GATT notifications and data
//                // Note: On Samsung devices, the connection must be done on main thread
//                mBluetoothGatt = device.connectGatt(ScanActivity.this, false, mGattCallback);
//
//                /*
//                There is also another simplest way to connect to a device. If you already stored
//                the device in a list (List<BluetoothDevice>) you can retrieve it directly and
//                connect to it:
//
//                mBluetoothGatt = mList.get(position).connectGatt(MainActivity.this, false, mGattCallback);
//                 */
//            }
//        });
//    }

    //Connection callback
    BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //Device connected, start discovering services
                        Log.i(TAG, "DEVICE CONNECTED. DISCOVERING SERVICES...");
                        mBluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //Device disconnected
                        Log.i(TAG, "DEVICE DISCONNECTED");
                    }
                }

                // On discover services method
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //Services discovered successfully. Start parsing services and characteristics
                        Log.i(TAG, "SERVICES DISCOVERED. PARSING...");
                        displayGattServices(gatt.getServices());
                    } else {
                        //Failed to discover services
                        Log.i(TAG, "FAILED TO DISCOVER SERVICES");
                    }
                }

                //When reading a characteristic, here you receive the task result and the value
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //READ WAS SUCCESSFUL
                        Log.i(TAG, "ON CHARACTERISTIC READ SUCCESSFUL");
                        //Read characteristic value like:
                        //characteristic.getValue();
                        //Which it returns a byte array. Convert it to HEX. string.
                    } else {
                        Log.i(TAG, "ERROR READING CHARACTERISTIC");
                    }
                }

                //When writing, here you can check whether the task was completed successfully or not
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "ON CHARACTERISTIC WRITE SUCCESSFUL");
                    } else {
                        Log.i(TAG, "ERROR WRITING CHARACTERISTIC");
                    }
                }

                //In this method you can read the new values from a received notification
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.i(TAG, "NEW NOTIFICATION RECEIVED");
                    //New notification received. Check the characteristic it comes from and parse to string
                    /*if(characteristic.getUuid().toString().contains("0000fff3")){
                        characteristic.getValue();
                    }*/
                }

                //RSSI values from the connection with the remote device are received here
                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                    Log.i(TAG, "NEW RSSI VALUE RECEIVED");
                    //Read remote RSSI like: mBluetoothGatt.readRemoteRssi();
                    //Here you get the gatt table where the rssi comes from, the rssi value and the
                    //status of the task.
                }
            };

    //Method which parses all services and characteristics from the GATT table.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        //Check if there is any gatt services. If not, return.
        if (gattServices == null) return;

        // Loop through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            Log.i(TAG, "SERVICE FOUND: "+gattService.getUuid().toString());
            //Loop through available characteristics for each service
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                Log.i(TAG, "  CHAR. FOUND: "+gattCharacteristic.getUuid().toString());
            }
        }

        //****************************************
        // CONNECTION PROCESS FINISHED!
        //****************************************
        Log.i(TAG, "*************************************");
        Log.i(TAG, "CONNECTION COMPLETED SUCCESFULLY");
        Log.i(TAG, "*************************************");

    }
}


/*

        One you have connected and discovered all services, you can start reading, writing and
        enabling notifications for the characteristics.

        First save on a var the characteristic you want in the loop above as:

        *************************************************************************
        * if(gattCharacteristic.getUuid().toString().contains("0000fff3")){     *
        *     BluetoothGattCharacteristic myChar = gattCharacteristic;          *
        * }                                                                     *
        *************************************************************************

        ####IMPORTANT: All the read, write and enable notification task must be done in a Thread.

        To read a characteristic simply use:

        *************************************************
        * mBluetoothGatt.readCharacteristic(myChar);    *
        *************************************************

        You will receive the read value and result in the mGattCallback above

        To write a characteristic, first set the value and then start the write task. Remember that
        the value must be a byte array

        ***************************************************
        * byte[] mValue = {0x01, 0x02};                   *
        * myChar.setValue(mValue);                        *
        * mBluetoothGatt.writeCharacteristic(myChar);     *
        ***************************************************

        To enable notifications fot a characteristics add:

        ***********************************************************************
        * mBluetoothGatt.setCharacteristicNotification(myChar, true/false);   *
        ***********************************************************************

        You also need to set the client characteristic configuration descriptor 0x2902

        *****************************************************************************
        * UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");      *
        * BluetoothGattDescriptor descriptor = myChar.getDescriptor(uuid);          *
        * descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);   *
        * mBluetoothGatt.writeDescriptor(descriptor);                               *
        *****************************************************************************

        You will receive the notifications in the mGattCallback above.

        A simple thread example to run this code could be this:

        *********************************************************
        * Thread writeThread = new Thread(writeThreadMethod);   *
        * writeThread.start();                                  *
        *********************************************************

        And the writeThreadMethod looks like this:

        *************************************************************
        * Thread writeThreadMethod = new Thread(new Runnable() {    *
        * @Override                                                 *
        * public void run() {                                       *
        *     //Add read, write or enable notifications here        *
        *     runOnUiThread(new Runnable() {                        *
        *         @Override                                         *
        *         public void run() {                               *
        *             //Add UI changes here                         *
        *             }                                             *
        *         });                                               *
        *     }                                                     *
        * });                                                       *
        *************************************************************

        In the next examples we will see how to register and check EID devices.
*/

