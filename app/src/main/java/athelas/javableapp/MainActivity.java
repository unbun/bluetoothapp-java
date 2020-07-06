package athelas.javableapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

// https://www.youtube.com/watch?v=y8R2C86BIUc&list=PLgCYzUzKIBE8KHMzpp6JITZ2JxTgWqDH2
// https://developer.android.com/reference/android/bluetooth/BluetoothDevice
// https://developer.android.com/guide/topics/connectivity/bluetooth

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private static final String appName = "MYAPP";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final UUID HC02_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    BluetoothDevice mBTDevice;

    BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothConnectionService mBTConnection;

    Button btnToggleDiscoverable;
    Button btnStartConnection;
    Button btnSend;
    EditText etSend;

    public ArrayList<BluetoothDevice> mBTDevicesList;
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    // Create a BroadcastReciever for ACTION_FOUND
    private final BroadcastReceiver mStateChangeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // when discovery finds a device
            if(action.equals(mBtAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBtAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mStateChangeReciever: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mStateChangeReciever: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mStateChangeReciever: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mStateChangeReciever: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mModeChangeReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mModeChangeReciever: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mModeChangeReciever: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mModeChangeReciever: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mModeChangeReciever: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mModeChangeReciever: Connected.");
                        break;
                }

            }
        }
    };

    private BroadcastReceiver mDiscoverReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "mDiscoverReciever: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevicesList.add(device);

                Log.d(TAG, "mDiscoverReciever: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevicesList);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBondReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "mBondReciever: BOND_BONDED with '" + device.getName() + "'.");
                    mBTDevice = device;
                }

                if(device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "mBondReciever: BOND_BONDING.");
                }

                if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "mBondReciever: BOND_NONE.");

                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBTDevicesList = new ArrayList<>();

        // UI Elements
        Button btnOnOff = (Button) findViewById(R.id.enableDisableBT);
        btnToggleDiscoverable = (Button) findViewById(R.id.enableDisableDiscoverable);
        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.editText);


        //Broadcastes when bond state changes (i.e. pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondReciever, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        lvNewDevices.setOnItemClickListener(MainActivity.this);

        btnOnOff.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDisableBT();
            }
        });

        // when you want to start the connection with the paired device
        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        // when you want to send text to the connection service
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBTConnection.write(bytes);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        unregisterReceiver(mStateChangeReciever);
        unregisterReceiver(mModeChangeReciever);
        unregisterReceiver(mDiscoverReciever);
        unregisterReceiver(mBondReciever);
    }

    // create method for starting a connection
    // will fail and crash if you haven't paired first (i.e. if mBTDevice is null)
    public void startConnection() {
        startBTConnection(mBTDevice, HC02_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initalizing RFCOM Bluetooth Connection.");
        mBTConnection.startClient(device, uuid);
    }

    public void enableDisableBT() {
        if(mBtAdapter == null) {
            Log.d(TAG, "enableDisableBT: Does not have BT capabilites.");
        }

        if(!mBtAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mStateChangeReciever, BTIntent);
        }

        if(mBtAdapter.isEnabled()) {
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBtAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mStateChangeReciever, BTIntent);
        }
    }


    public void enableDisableDiscoverability(View view) {
        Log.d(TAG, "enableDisableDiscoverability: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mModeChangeReciever, intentFilter);
    }

    public void btDiscover(View view) {
        Log.d(TAG, "btDiscover: Looking for unpaired devices.");

        if(mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
            Log.d(TAG, "btDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();
            mBtAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDiscoverReciever, discoverDevicesIntent);
        }

        if(!mBtAdapter.isDiscovering()) {
            //check BT permissions in manifest
            checkBTPermissions();
            mBtAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mDiscoverReciever, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > M because it is not needed otherwise.
     */
    @SuppressLint("NewApi")
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < Lollipop.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBtAdapter.cancelDiscovery(); // memory intensive

        Log.d(TAG, "onItemClick: Clicked a bluetooth device");
        String deviceName = mBTDevicesList.get(i).getName();
        String deviceAddress = mBTDevicesList.get(i).getAddress();

        Log.d(TAG, "onItemClick: device: '" + deviceName +"', " + deviceAddress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevicesList.get(i).createBond();

            mBTDevice = mBTDevicesList.get(i);
            mBTConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }
}