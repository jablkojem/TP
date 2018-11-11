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
import android.content.Intent;
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

    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothGatt mBluetoothGatt;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
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
            Toast.makeText(getBaseContext(), "ide", Toast.LENGTH_SHORT).show();

//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//
//                if (mConnectionState != STATE_CONNECTED) {
//                    // TODO sane release
//                    PowerManager.WakeLock wl = JoH.getWakeLock("bluetooth-meter-connected", 60000);
//                    mConnectionState = STATE_CONNECTED;
//                    mLastConnectedDeviceAddress = gatt.getDevice().getAddress();
//
//                    statusUpdate("Connected to device: " + mLastConnectedDeviceAddress);
//                    if ((playSounds() && (JoH.ratelimit("bt_meter_connect_sound", 3)))) {
//                        JoH.playResourceAudio(R.raw.bt_meter_connect);
//                    }
//
//                    Log.d(TAG, "Delay for settling");
//                    waitFor(600);
//                    statusUpdate("Discovering services");
//                    service_discovery_count = 0; // reset as new non retried connnection
//                    discover_services();
//                    // Bluetooth_CMD.poll_queue(); // do we poll here or on service discovery - should we clear here?
//                } else {
//                    // TODO timeout
//                    Log.e(TAG, "Apparently already connected - ignoring");
//                }
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                final int old_connection_state = mConnectionState;
//                mConnectionState = STATE_DISCONNECTED;
//                statusUpdate("Disconnected");
//                if ((old_connection_state == STATE_CONNECTED) && (playSounds() && (JoH.ratelimit("bt_meter_disconnect_sound", 3)))) {
//                    JoH.playResourceAudio(R.raw.bt_meter_disconnect);
//                }
//                close();
//                refreshDeviceCache(mBluetoothGatt);
//                Bluetooth_CMD.poll_queue();
//                // attempt reconnect
//                reconnect();
//            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Toast.makeText(getBaseContext(), "ide", Toast.LENGTH_SHORT).show();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                services_discovered = true;
//                statusUpdate("Services discovered");
//
//                bondingstate = mBluetoothGatt.getDevice().getBondState();
//                if (bondingstate != BluetoothDevice.BOND_BONDED) {
//                    statusUpdate("Attempting to create pairing bond - device must be in pairing mode!");
//                    sendDeviceUpdate(gatt.getDevice());
//                    mBluetoothGatt.getDevice().createBond();
//                    waitFor(1000);
//                    bondingstate = mBluetoothGatt.getDevice().getBondState();
//                    if (bondingstate != BluetoothDevice.BOND_BONDED) {
//                        statusUpdate("Pairing appeared to fail");
//                    } else {
//                        sendDeviceUpdate(gatt.getDevice());
//                    }
//                } else {
//                    Log.d(TAG, "Device is already bonded - good");
//                }
//
//                if (d) {
//                    List<BluetoothGattService> gatts = getSupportedGattServices();
//                    for (BluetoothGattService bgs : gatts) {
//                        Log.d(TAG, "DEBUG: " + bgs.getUuid());
//                    }
//                }
//
//                if (queue.isEmpty()) {
//                    statusUpdate("Requesting data from meter");
//                    Bluetooth_CMD.read(DEVICE_INFO_SERVICE, MANUFACTURER_NAME, "get device manufacturer");
//                    Bluetooth_CMD.read(CURRENT_TIME_SERVICE, TIME_CHARACTERISTIC, "get device time");
//
//                    Bluetooth_CMD.notify(GLUCOSE_SERVICE, GLUCOSE_CHARACTERISTIC, "notify new glucose record");
//                    Bluetooth_CMD.enable_notification_value(GLUCOSE_SERVICE, GLUCOSE_CHARACTERISTIC, "notify new glucose value");
//
//                    Bluetooth_CMD.enable_notification_value(GLUCOSE_SERVICE, CONTEXT_CHARACTERISTIC, "notify new context value");
//                    Bluetooth_CMD.notify(GLUCOSE_SERVICE, CONTEXT_CHARACTERISTIC, "notify new glucose context");
//
//                    Bluetooth_CMD.enable_indications(GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, "readings indication request");
//                    Bluetooth_CMD.notify(GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, "notify glucose record");
//                    Bluetooth_CMD.write(GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, RecordsCmdTx.getAllRecords(), "request all readings");
//                    Bluetooth_CMD.notify(GLUCOSE_SERVICE, GLUCOSE_CHARACTERISTIC, "notify new glucose record again"); // dummy
//
//                    Bluetooth_CMD.poll_queue();
//
//                } else {
//                    Log.e(TAG, "Queue is not empty so not scheduling anything..");
//                }
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {
            Toast.makeText(getBaseContext(), "ide", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Descriptor written to: " + descriptor.getUuid() + " getvalue: " + JoH.bytesToHex(descriptor.getValue()) + " status: " + status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Bluetooth_CMD.poll_queue();
//            } else {
//                Log.e(TAG, "Got gatt descriptor write failure: " + status);
//                Bluetooth_CMD.retry_last_command(status);
//            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            Toast.makeText(getBaseContext(), "ide", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Written to: " + characteristic.getUuid() + " getvalue: " + JoH.bytesToHex(characteristic.getValue()) + " status: " + status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (ack_blocking()) {
//                    if (d)
//                        Log.d(TAG, "Awaiting ACK before next command: " + awaiting_ack + ":" + awaiting_data);
//                } else {
//                    Bluetooth_CMD.poll_queue();
//                }
//            } else {
//                Log.e(TAG, "Got gatt write failure: " + status);
//                Bluetooth_CMD.retry_last_command(status);
//            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Toast.makeText(getBaseContext(), "ide", Toast.LENGTH_SHORT).show();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//
//                if (characteristic.getUuid().equals(TIME_CHARACTERISTIC)) {
//                    UserError.Log.d(TAG, "Got time characteristic read data");
//                    ct = new CurrentTimeRx(characteristic.getValue());
//                    statusUpdate("Device time: " + ct.toNiceString());
//                } else if (characteristic.getUuid().equals(DATE_TIME_CHARACTERISTIC)) {
//                    UserError.Log.d(TAG, "Got date time characteristic read data");
//                    ct = new CurrentTimeRx(characteristic.getValue());
//                    statusUpdate("Device time: " + ct.toNiceString());
//                } else if (characteristic.getUuid().equals(MANUFACTURER_NAME)) {
//                    mLastManufacturer = characteristic.getStringValue(0);
//                    UserError.Log.d(TAG, "Manufacturer Name: " + mLastManufacturer);
//                    statusUpdate("Device from: " + mLastManufacturer);
//
//                    await_acks = false; // reset
//
//                    // Roche Aviva Connect uses a DateTime characteristic instead
//                    if (mLastManufacturer.startsWith("Roche")) {
//                        Bluetooth_CMD.transmute_command(CURRENT_TIME_SERVICE, TIME_CHARACTERISTIC,
//                                GLUCOSE_SERVICE, DATE_TIME_CHARACTERISTIC);
//                    }
//
//                    // Diamond Mobile Mini DM30b firmware v1.2.4
//                    // v1.2.4 has reversed sequence numbers and first item is last item and no clock access
//                    if (mLastManufacturer.startsWith("TaiDoc")) {
//                        // no time service!
//                        Bluetooth_CMD.delete_command(CURRENT_TIME_SERVICE, TIME_CHARACTERISTIC);
//                        ct = new CurrentTimeRx(); // implicitly trust meter time stamps!! beware daylight saving time changes
//                        ct.noClockAccess = true;
//                        ct.sequenceNotReliable = true;
//
//                        // no glucose context!
//                        Bluetooth_CMD.delete_command(GLUCOSE_SERVICE, CONTEXT_CHARACTERISTIC);
//                        Bluetooth_CMD.delete_command(GLUCOSE_SERVICE, CONTEXT_CHARACTERISTIC);
//
//                        // only request last reading - diamond mini seems to make sequence 0 be the most recent record
//                        Bluetooth_CMD.replace_command(GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, "W",
//                                new Bluetooth_CMD("W", GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, RecordsCmdTx.getFirstRecord(), "request newest reading"));
//
//                    }
//
//                    // Caresens Dual
//                    if (mLastManufacturer.startsWith("i-SENS")) {
//                        Bluetooth_CMD.delete_command(CURRENT_TIME_SERVICE, TIME_CHARACTERISTIC);
//                        ct = new CurrentTimeRx(); // implicitly trust meter time stamps!! beware daylight saving time changes
//                        ct.noClockAccess = true;
//                        Bluetooth_CMD.notify(ISENS_TIME_SERVICE, ISENS_TIME_CHARACTERISTIC, "notify isens clock");
//                        Bluetooth_CMD.write(ISENS_TIME_SERVICE, ISENS_TIME_CHARACTERISTIC, new TimeTx(JoH.tsl()).getByteSequence(), "set isens clock");
//                        Bluetooth_CMD.write(ISENS_TIME_SERVICE, ISENS_TIME_CHARACTERISTIC, new TimeTx(JoH.tsl()).getByteSequence(), "set isens clock");
//
//                        Bluetooth_CMD.replace_command(GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, "W",
//                                new Bluetooth_CMD("W", GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, RecordsCmdTx.getNewerThanSequence(getHighestSequence()), "request reading newer than " + getHighestSequence()));
//
//                    }
//
//                    // LifeScan Verio Flex
//                    if (mLastManufacturer.startsWith("LifeScan")) {
//
//                        await_acks = true;
//
//                        Bluetooth_CMD.empty_queue(); // Verio Flex isn't standards compliant
//
//                        Bluetooth_CMD.notify(VERIO_F7A1_SERVICE, VERIO_F7A3_NOTIFICATION, "verio general notification");
//                        Bluetooth_CMD.enable_notification_value(VERIO_F7A1_SERVICE, VERIO_F7A3_NOTIFICATION, "verio general notify value");
//                        Bluetooth_CMD.write(VERIO_F7A1_SERVICE, VERIO_F7A2_WRITE, VerioHelper.getTimeCMD(), "verio ask time");
//                        Bluetooth_CMD.write(VERIO_F7A1_SERVICE, VERIO_F7A2_WRITE, VerioHelper.getTcounterCMD(), "verio T data query"); // don't change order with R
//                        Bluetooth_CMD.write(VERIO_F7A1_SERVICE, VERIO_F7A2_WRITE, VerioHelper.getRcounterCMD(), "verio R data query"); // don't change order with T
//
//                    }
//
//                } else {
//                    Log.d(TAG, "Got a different charactersitic! " + characteristic.getUuid().toString());
//
//                }
//                Bluetooth_CMD.poll_queue();
//            } else {
//                Log.e(TAG, "Got gatt read failure: " + status);
//                Bluetooth_CMD.retry_last_command(status);
//            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Toast.makeText(getBaseContext(), "ide", Toast.LENGTH_SHORT).show();
//
//            final PowerManager.WakeLock wl = JoH.getWakeLock("bt-meter-characterstic-change", 30000);
//            try {
//                processCharacteristicChange(gatt, characteristic);
//                Bluetooth_CMD.poll_queue();
//            } finally {
//                JoH.releaseWakeLock(wl);
//            }
        }
    };


    public synchronized void getDataThermo() throws InterruptedException {
        BluetoothManager mBluetoothManage = (BluetoothManager) getSystemService(getApplicationContext().BLUETOOTH_SERVICE);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();


//        if (mBluetoothManage != null) {
//            BluetoothAdapter mBluetoothAdapter = mBluetoothManage.getAdapter();
//        } else {
//            Toast.makeText(getBaseContext(), "bluetooth manager not good", Toast.LENGTH_SHORT).show();
//        }


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

            getDataThermo();

        }
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
