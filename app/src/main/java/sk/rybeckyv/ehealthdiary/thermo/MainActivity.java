package sk.rybeckyv.ehealthdiary.thermo;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyInterface {

    private final static String TAG = MainActivity.class.getSimpleName();
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean is_connected = false;
    SharedPreferences prefs;

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String HM_10_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public MainActivity dexCollectionService;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothDevice device;

    private Context mContext = null;

    private static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING;
    private static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    private static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;

    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static UUID xDripDataService = UUID.fromString(HM_10_SERVICE);
    public final static UUID xDripDataCharacteristic = UUID.fromString(HM_RX_TX);





    private final int REQUEST_ENABLE_BT = 1;
//    private BluetoothGatt mBluetoothGatt;
//
//    private BluetoothAdapter mBluetoothAdapter;
//    private boolean mScanning;
    private Handler mHandler;
    private List<BluetoothDevice> bluetoothDevices;
    private BluetoothDevice myDevice;
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            Toast.makeText(getApplicationContext(), bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
            if (bluetoothDevice.getAddress() == "C5:FA:D0:90:A6:8C") {
                myDevice = bluetoothDevice;
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothDevices.add(device);
                        }
                    });
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    /*
    Ob1g5statemachine.java – line 430
Ob1g5statemachine.java – line 850


new sensor started
     */

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
//                ActiveBluetoothDevice.connected();
                Log.w(TAG, "Connected to GATT server.");
                Toast.makeText(getBaseContext(), "Connected to GATT server", Toast.LENGTH_SHORT).show();
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
//                ActiveBluetoothDevice.disconnected();
                Log.w(TAG, "Disconnected from GATT server.");
                Toast.makeText(getBaseContext(), "Disconnected from GATT server", Toast.LENGTH_SHORT).show();

//                setRetryTimer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Toast.makeText(getBaseContext(), "service discovered", Toast.LENGTH_SHORT).show();


            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService gattService = mBluetoothGatt.getService(xDripDataService);
                if (gattService != null) {
                    BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(xDripDataCharacteristic);
                    if (gattCharacteristic != null ) {
                        final int charaProp = gattCharacteristic.getProperties();

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                        } else {
                            Log.e(TAG, "characteristic " + gattCharacteristic.getUuid() + " doesn't have notify properties");
                            Toast.makeText(getBaseContext(), "characteristic " + gattCharacteristic.getUuid() + " doesn't have notify properties", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Log.e(TAG, "characteristic " + xDripDataCharacteristic + " not found");

                    }
                } else {
                    Log.e(TAG, "service " + xDripDataCharacteristic + " not found");
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();

            Toast.makeText(getBaseContext(), "characteristic changed", Toast.LENGTH_SHORT).show();

//            if (data != null && data.length > 0) { setSerialDataToTransmitterRawData(data, data.length); }
        }
    };

    public synchronized void getDataThermo() throws InterruptedException {
        BluetoothManager mBluetoothManage = (BluetoothManager) getSystemService(getApplicationContext().BLUETOOTH_SERVICE);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (!getPackageManager().hasSystemFeature(getPackageManager().FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getBaseContext(), "ble not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getBaseContext(), "activity started", Toast.LENGTH_SHORT).show();
        }

        bluetoothDevices = new ArrayList<>();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        Thread.sleep(5000);
        Toast.makeText(getBaseContext(), "list " + bluetoothDevices.toString(), Toast.LENGTH_SHORT).show();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);


        String macAddr = "";

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (deviceName.contains("Dexcom")) {
                    macAddr = deviceHardwareAddress;
                }
            }
        } else {
            Toast.makeText(getBaseContext(), "No devices paired", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(getBaseContext(), "mac addr " + macAddr, Toast.LENGTH_SHORT).show();

        if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getRemoteDevice(macAddr) != null) {
            Toast.makeText(getBaseContext(), "connecting dev " + macAddr, Toast.LENGTH_SHORT).show();
            if (mBluetoothAdapter == null || macAddr == null) {
                Toast.makeText(getBaseContext(), "BluetoothAdapter not initialized or unspecified address.", Toast.LENGTH_SHORT).show();
            }
            if (mBluetoothGatt != null) {
                Toast.makeText(getBaseContext(), "BGatt isnt null.", Toast.LENGTH_SHORT).show();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
            BluetoothDevice dev = mBluetoothAdapter.getRemoteDevice(macAddr);
            dev.setPin("000000".getBytes());
            if (dev == null) {
                Toast.makeText(getBaseContext(), "Device not found.  Unable to connect.", Toast.LENGTH_SHORT).show();
            }
            mBluetoothGatt = dev.connectGatt(this, true, mGattCallback);

            if (mBluetoothGatt == null) {
                Toast.makeText(getBaseContext(), "bluetooth gatt null", Toast.LENGTH_SHORT).show();
            }
            try {
                Method localMethod = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    Boolean aa = (Boolean) localMethod.invoke(mBluetoothGatt, new Object[0]);
                    Toast.makeText(getBaseContext(), "refresh " + aa, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception localException) {
                Toast.makeText(getBaseContext(), "An exception occured while refreshing device", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(getBaseContext(), "End", Toast.LENGTH_SHORT).show();
        }
    }

    public void runMeasure(View view) throws InterruptedException {
        Button button = (Button) view;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getBaseContext(), "Device not support bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        } else {

//            getDataThermo();

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    if (deviceName.contains("Dexcom")) {
                        attemptConnection(device);
                    }
                }
            }


        }
    }

    public void attemptConnection(BluetoothDevice btDevice) {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter != null) {
                if (device != null) {
                    Toast.makeText(getBaseContext(), "STATE_DISCONNECTED", Toast.LENGTH_SHORT).show();
                    mConnectionState = STATE_DISCONNECTED;
                    for (BluetoothDevice bluetoothDevice : mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT)) {
                        if (bluetoothDevice.getAddress().compareTo(device.getAddress()) == 0) {
                            mConnectionState = STATE_CONNECTED;
                            Toast.makeText(getBaseContext(), "STATE_CONNECTED gatt", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                Log.w(TAG, "Connection state: " + mConnectionState);
                if (mConnectionState == STATE_DISCONNECTED || mConnectionState == STATE_DISCONNECTING) {
                    if (btDevice != null) {
                        mDeviceName = btDevice.getName();
                        mDeviceAddress = btDevice.getAddress();
                        if (mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getRemoteDevice(mDeviceAddress) != null) {
                            Toast.makeText(getBaseContext(), "is going to connect", Toast.LENGTH_SHORT).show();
                            connect(mDeviceAddress);
                            Toast.makeText(getBaseContext(), "after connect", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } else if (mConnectionState == STATE_CONNECTED) { //WOOO, we are good to go, nothing to do here!
                    Log.w(TAG, "Looks like we are already connected, going to read!");
                    Toast.makeText(getBaseContext(), "Looks like we are already connected, going to read!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
//        setRetryTimer();
    }

    public boolean connect(final String address) {
        Log.w(TAG, "going to connect to device at address" + address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            Toast.makeText(getBaseContext(), "BluetoothAdapter not initialized or unspecified address.", Toast.LENGTH_SHORT).show();
//            setRetryTimer();
            return false;
        }
        if (mBluetoothGatt != null) {
            Toast.makeText(getBaseContext(), "BGatt isnt null, Closing.", Toast.LENGTH_SHORT).show();

            Log.w(TAG, "BGatt isnt null, Closing.");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Toast.makeText(getBaseContext(), "Device not found.  Unable to connect.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Device not found.  Unable to connect.");
//            setRetryTimer();
            return false;
        }
        Log.w(TAG, "Trying to create a new connection.");
        Toast.makeText(getBaseContext(), "Trying to create a new connection.", Toast.LENGTH_SHORT).show();

        mBluetoothGatt = device.connectGatt(getApplicationContext(), true, mGattCallback);
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void send(View view) {
        Button button = (Button) view;

        onThermoUpdated(0.0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Toast.makeText(getBaseContext(), "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                try {
                    getDataThermo();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onThermoUpdated(double thermo) {

        Toast.makeText(getBaseContext(), "Updateing to server", Toast.LENGTH_SHORT).show();
        TextView textView = (TextView) findViewById(R.id.value);
        textView.setText(getString(R.string.thermo_value, thermo));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//        Map<String, String> params = new HashMap<>();
////        params.put("id", UUID.randomUUID().toString());
//        params.put("table", "DEXCOMG5");
//        params.put("glukoza", "121");
////        params.put("datum", sdf.format(new Date()));

        Map<String, String> params = new HashMap<>();
        params.put("id", UUID.randomUUID().toString());
        params.put("table", "DEXC");
        params.put("glukoza", "127");
        params.put("datum", sdf.format(new Date()));

        Toast.makeText(getBaseContext(), "id length " + UUID.randomUUID().toString().length(), Toast.LENGTH_SHORT).show();


        JSONObject object = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://147.175.98.38/add_value_t.php", object,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("success")) {
                            Toast.makeText(getBaseContext(), "Data has been saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), "Data saving error "+error.toString(), Toast.LENGTH_SHORT).show();

                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getBaseContext());
        queue.add(request);
    }
}
