package com.joesnason.blescanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {


    private static String TAG = "BLEScanner";

    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;

    private ListView mListView;

    ArrayAdapter<String> mAdapter;
    ArrayList<String> listItems=new ArrayList<String>();


    private Handler scanhandler = null;

    final Runnable scanRunable  = new Runnable() {
        @Override
        public void run() {

            if (!mBluetoothAdapter.isEnabled() ) {

                Intent mIntentOpenBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(mIntentOpenBT, REQUEST_ENABLE_BT);

                Log.d(TAG, "enable Bluetooth");

            } else {
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                Log.d(TAG, "start Le scan");

            }
            scanhandler.postDelayed(stopscanRunable,1000);


        }
    };

    final Runnable stopscanRunable = new Runnable() {
        @Override
        public void run() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            listItems.clear();

            scanhandler.postDelayed(scanRunable, 1000);
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate start");
        setContentView(R.layout.activity_main);

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device not support Bluetooth", Toast.LENGTH_LONG);
        }

        mListView = (ListView) findViewById(R.id.listview);



        mAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);

        mListView.setAdapter(mAdapter);

        scanhandler = new Handler();


    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume start");

        scanhandler.post(scanRunable);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            mBluetoothAdapter.startLeScan(mLeScanCallback);
                Log.d(TAG, "start scan");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();


        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                // 取得藍芽裝置
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "device name: " + device.getName());


            }
        }

    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            Log.d(TAG,"enter Scan callback");
            int startByte = 2;
            boolean patternFound = false;
            String uuid = null;
            int major = 0 , minor = 0;
            int Txpower = 0;

            /* Because advertisement packet
                | 0x02 | 0x15     | - - - - - - - - - - - - - - - - - - - | - - - - | - - - - | - - - - |
                |  ID  | Data Len |    uuid                               |   major |  minor  | Tx power|
                0      1          2                                      18        20        22        24

            example :
                02 | 15 | E2 0A 39 F4 73 F5 4B C4 A1 2F 17 D1 AD 07 A9 61 | 00 00 | 00 00 | C8
             */
            while (startByte <= 5) {
                if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound) {
                //Convert to hex String
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                String hexString = Util.bytesToHex(uuidBytes);

                //Here is your UUID

                uuid = String.format("%s-%s-%s-%s-%s",hexString.substring(0,8),hexString.substring(8,12),hexString.substring(12,16),hexString.substring(16,20),hexString.substring(20,32));

                //Here is your Major value
                major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

                //Here is your Minor value
                minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

                Txpower = scanRecord[startByte+ 24];
            }

            Beacon beacon = new Beacon(uuid,device.getName(),major,minor,rssi);

            Log.d(TAG, "Device name: " + beacon.getName() + " UUID: " + beacon.getUUID() + "  Major: " + beacon.getMajor() + " Minor: " + beacon.getMinor() + " rssi: " + beacon.getRssi() + " power: " + Txpower);

            String item = "Device name: " + beacon.getName() + "\nUUID: " + beacon.getUUID() + "\nMajor: " + beacon.getMajor() + "\nMinor: " + beacon.getMinor() + "\nrssi: " + rssi;

            double Distance = calucateDistance(rssi,Txpower);
            Log.d(TAG, "Distance: " + Distance);


            if (!listItems.contains(item)){
                listItems.add(item);
                //mAdapter.add(item);
                mAdapter.notifyDataSetChanged();
            }
        }


        private double calucateDistance(int rssi, int TxPower){


            double ratio = ((float)rssi) / TxPower;
            Log.d(TAG,"ratio: " + ratio);
            // less than Txpower,  it mean the Beacon in  ONE Miles range. the distance and rssi are linear relationship.
            if(ratio < 1.0){
                return Math.pow(ratio, 10);
            }

            return 0.89976 * Math.pow(ratio, 7.7095) + 0.111;
        }


    };


}
