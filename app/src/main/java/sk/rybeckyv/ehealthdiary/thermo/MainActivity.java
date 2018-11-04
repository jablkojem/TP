package sk.rybeckyv.ehealthdiary.thermo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyInterface {

    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothGatt mBluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    /*
    Ob1g5statemachine.java – line 430
Ob1g5statemachine.java – line 850

     */

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Toast.makeText(getBaseContext(), "onConnectionStateChange::STATE_CONNECTED", Toast.LENGTH_SHORT).show();
//                mBluetoothGatt = gatt;
//                mConnectionState = STATE_CONNECTED;
//                ActiveBluetoothDevice.connected();
//                Log.i(TAG, "Connected to GATT server.");
//                Log.i(TAG, "Connection state: Bonded - " + device.getBondState());
//
//                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//                    currentGattTask = GATT_SETUP;
//                    mBluetoothGatt.discoverServices();
//
//                } else {
//                    device.setPin("000000".getBytes());
//                    device.createBond();
//                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Toast.makeText(getBaseContext(), "onConnectionStateChange::STATE_DISCONNECTED", Toast.LENGTH_SHORT).show();

//                mConnectionState = STATE_DISCONNECTED;
//                ActiveBluetoothDevice.disconnected();
//                Log.w(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Toast.makeText(getBaseContext(), "onServicesDiscovered", Toast.LENGTH_SHORT).show();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Toast.makeText(getBaseContext(), "onServicesDiscovered::GATT_SUCCESS", Toast.LENGTH_SHORT).show();
//                Log.i(TAG, "Services Discovered: " + status);
//                authenticateConnection(gatt);
//
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Toast.makeText(getBaseContext(), "onCharacteristicRead", Toast.LENGTH_SHORT).show();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Toast.makeText(getBaseContext(), "onCharacteristicRead::GATT_SUCCESS", Toast.LENGTH_SHORT).show();

//                Log.i(TAG, "Characteristic Read");
//                byte[] value = characteristic.getValue();
//                if(value != null) {
//                    Log.i(TAG, "VALUE" + value);
//                } else {
//                    Log.w(TAG, "Characteristic was null");
//                }
//                nextGattStep();
            } else {
                Toast.makeText(getBaseContext(), "onCharacteristicRead::GATT_ELSE", Toast.LENGTH_SHORT).show();

//                Log.w(TAG, "Characteristic failed to read");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Toast.makeText(getBaseContext(), "onCharacteristicChanged", Toast.LENGTH_SHORT).show();

////            Log.i(TAG, "Characteristic changed");
//            UUID charUuid = characteristic.getUuid();
////            Log.i(TAG, "Characteristic Update Received: " + charUuid);
//            if(charUuid.compareTo(mResponseCharacteristic.getUuid()) == 0) {
////                Log.i(TAG, "mResponseCharacteristic Update");
//            }
//            if(charUuid.compareTo(mCommandCharacteristic.getUuid()) == 0) {
////                Log.i(TAG, "mCommandCharacteristic Update");
//            }
//            if(charUuid.compareTo(mHeartBeatCharacteristic.getUuid()) == 0) {
////                Log.i(TAG, "mHeartBeatCharacteristic Update");
//            }
//            if(charUuid.compareTo(mReceiveDataCharacteristic.getUuid()) == 0) {
////                Log.i(TAG, "mReceiveDataCharacteristic Update");
////                byte[] value = characteristic.getValue();
////                if(value != null) {
////                    Log.i(TAG, "Characteristic: " + value);
////                    Log.i(TAG, "Characteristic: " + value.toString());
////                    Log.i(TAG, "Characteristic getstring: " + characteristic.getStringValue(0));
////                    Log.i(TAG, "SUBSCRIBED TO RESPONSE LISTENER");
////                    Observable.just(characteristic.getValue()).subscribe(mDataResponseListener);
////                } else {
////                    Log.w(TAG, "Characteristic was null");
////                }
//            }
//            Log.i(TAG, "NEW VALUE: " + characteristic.getValue().toString());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Toast.makeText(getBaseContext(), "onDescriptorWrite", Toast.LENGTH_SHORT).show();

//            Log.i(TAG, "Wrote a discriptor, status: " + status);
//            if(step == 2 && currentGattTask == GATT_SETUP) {
//                setListeners(2);
//            } else if(step == 3) {
//                setListeners(3);
//            } else if(step == 4) {
//                setListeners(4);
//            } else if(step == 5) {
//                Log.i(TAG, "Done setting Listeners");
//            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Toast.makeText(getBaseContext(), "onCharacteristicWrite", Toast.LENGTH_SHORT).show();

//            Log.i(TAG, "Wrote a characteristic: " + status);
//            nextGattStep();
        }
    };

    public void getDataThermo() {


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (device.getName().contains("Dexcom")) {

                    mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
//
//                    Toast.makeText(getBaseContext(), "MAC address : " + deviceHardwareAddress, Toast.LENGTH_SHORT).show();
//
//
//                    ParcelUuid[] uuids = device.getUuids();
//                    Toast.makeText(getBaseContext(), "UUIDs : " + Arrays.toString(uuids), Toast.LENGTH_SHORT).show();
//
//                    Toast.makeText(getBaseContext(), "Device " + deviceName + " is going to be connected", Toast.LENGTH_SHORT).show();
//                    ConnectedThread thread = new ConnectedThread(this, device, this);
//                    thread.execute();
                }

            }
        } else {
            Toast.makeText(getBaseContext(), "No devices paired", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getBaseContext(), "End", Toast.LENGTH_SHORT).show();

    }

    public void runMeasure(View view) {
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


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Toast.makeText(getBaseContext(), "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                getDataThermo();
                break;
        }
    }

    @Override
    public void onThermoUpdated(double thermo) {
        TextView textView = (TextView) findViewById(R.id.value);
        textView.setText(getString(R.string.thermo_value, thermo));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, String> params = new HashMap<>();
        params.put("id", UUID.randomUUID().toString());
        params.put("table", "GLUKOMER");
        params.put("glukoza", String.format("%.2f", thermo));
        params.put("datum", sdf.format(new Date()));

        JSONObject object = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://147.175.98.38/timak2/api/add_value_t.php", object,

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
                        Toast.makeText(getBaseContext(), "Data saving error", Toast.LENGTH_SHORT).show();

                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getBaseContext());
        queue.add(request);
    }
}
