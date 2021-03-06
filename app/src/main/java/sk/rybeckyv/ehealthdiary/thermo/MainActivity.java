package sk.rybeckyv.ehealthdiary.thermo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;

/* 

    --------------------------------
    Service uuid : 00001800-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a00-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a01-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a04-0000-1000-8000-00805f9b34fb
    --------------------------------
    Service uuid : 00001801-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a05-0000-1000-8000-00805f9b34fb
    --------------------------------
    Service uuid : f8083532-849e-531c-c594-30f1f86a4ea5
    Characteristic uuid : f8083533-849e-531c-c594-30f1f86a4ea5
    Characteristic uuid : f8083534-849e-531c-c594-30f1f86a4ea5
    Characteristic uuid : f8083535-849e-531c-c594-30f1f86a4ea5
    Characteristic uuid : f8083536-849e-531c-c594-30f1f86a4ea5
    Characteristic uuid : f8083537-849e-531c-c594-30f1f86a4ea5
    --------------------------------
    Service uuid : 0000180a-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a29-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a24-0000-1000-8000-00805f9b34fb
    Characteristic uuid : 00002a26-0000-1000-8000-00805f9b34fb
 */

public class MainActivity extends AppCompatActivity implements MyInterface {

    private int counter = 0;
    private final int REQUEST_ENABLE_BT = 1;
    public final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
    //    public static final UUID Control = UUID.fromString("F8083534-849E-531C-C594-30F1F86A4EA5");
//    public static final UUID CharacteristicE = UUID.fromString("F8084533-849E-531C-C594-30F1F86A4EA5");
    public static final UUID CGMService = UUID.fromString("F8083532-849E-531C-C594-30F1F86A4EA5");
    //    public static final UUID Authentication = UUID.fromString("F8083535-849E-531C-C594-30F1F86A4EA5");
    private static final UUID GLUCOSE_SERVICE = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
    public static final UUID TRANSMITER_PL_SERVICE = UUID.fromString("c97433f0-be8f-4dc8-b6f0-5343e6100eb4");
    public static final UUID HM_10_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final UUID blukonDataService = UUID.fromString("436a62c0-082e-4ce8-a08b-01d81f195b24");
    public final UUID nrfDataService = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public final UUID majo = UUID.fromString("0000febc-0000-1000-8000-00805f9b34fb");
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";


    public static final UUID GLUCOSE_CHARACTERISTIC = UUID.fromString("00002a18-0000-1000-8000-00805f9b34fb");
    public static final UUID Communication = UUID.fromString("F8083533-849E-531C-C594-30F1F86A4EA5");
    public static final UUID Control = UUID.fromString("F8083534-849E-531C-C594-30F1F86A4EA5");
    public static final UUID Authentication = UUID.fromString("F8083535-849E-531C-C594-30F1F86A4EA5");
    public static final UUID ProbablyBackfill = UUID.fromString("F8083536-849E-531C-C594-30F1F86A4EA5");


    public static final UUID CharacteristicUpdateNotification = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 2;

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            if (newState == STATE_DISCONNECTED) {
                System.out.println("STATE: state disconnected ");

//                Toast.makeText(getBaseContext(), "STATE DISCONNECTED", Toast.LENGTH_SHORT).show();
//                intentAction = ACTION_GATT_DISCONNECTED;
//                broadcastUpdate(intentAction);
            } else if (newState == STATE_CONNECTED) {
                System.out.println("STATE: state connected ");

//                intentAction = ACTION_GATT_CONNECTED;
//                broadcastUpdate(intentAction);
//                try {
//                    Thread.sleep(600);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                try {
                    Boolean ff = gatt.discoverServices();
                    System.out.println("discover services ff " + ff);
                } catch (RuntimeException e) {
                    System.out.println("RUNTIME EXCEPTION : " + e.toString());
                }
            }

//            RxBleLog.d("onConnectionStateChange newState=%d status=%d", newState, status);
//            nativeCallbackDispatcher.notifyNativeConnectionStateCallback(gatt, status, newState);
//            super.onConnectionStateChange(gatt, status, newState);
//            bluetoothGattProvider.updateBluetoothGatt(gatt);
//
//            if (isDisconnectedOrDisconnecting(newState)) {
//                disconnectionRouter.onDisconnectedException(new BleDisconnectedException(gatt.getDevice().getAddress()));
//            } else if (status != BluetoothGatt.GATT_SUCCESS) {
//                disconnectionRouter.onGattConnectionStateException(
//                        new BleGattException(gatt, status, BleGattOperationType.CONNECTION_STATE)
//                );
//            }
//
//            connectionStatePublishRelay.call(mapConnectionStateToRxBleConnectionStatus(newState));
        }

        private boolean isDisconnectedOrDisconnecting(int newState) {
            return newState == BluetoothGatt.STATE_DISCONNECTED || newState == BluetoothGatt.STATE_DISCONNECTING;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("STATE: onServicesDiscovered");

            List<BluetoothGattService> services = gatt.getServices();
            System.out.println("SERVICES : " + services.toString());
            System.out.println("SERVICES SIZE: " + services.size());
            for (BluetoothGattService service : services) {
                System.out.println("--------------------------------");
                System.out.println("Service uuid : " + service.getUuid());
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    System.out.println("Characteristic uuid : " + characteristic.getUuid());
//                    gatt.setCharacteristicNotification(characteristic, true);
//
//
//                    BluetoothGattDescriptor descriptor =
//                            characteristic.getDescriptor(CharacteristicUpdateNotification);
//
//                    descriptor.setValue(
//                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                    gatt.writeDescriptor(descriptor);
//                    System.out.println("descriptor enabled");
                }
            }
            System.out.println("--------------------------------");

            BluetoothGattService cgmService = gatt.getService(CGMService);
//            BluetoothGattCharacteristic characteristic = cgmService.getCharacteristic(Authentication);
            BluetoothGattCharacteristic characteristic = cgmService.getCharacteristic(Control);
            gatt.setCharacteristicNotification(characteristic, true);


            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();

            System.out.println("---------------------------");
            for (BluetoothGattDescriptor desc : descriptors) {
                System.out.println(desc.getUuid().toString());
            }
            System.out.println("---------------------------");

            BluetoothGattDescriptor descriptor =
                    characteristic.getDescriptor(descriptors.get(0).getUuid());

            if (descriptor != null) {
                descriptor.setValue(
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                System.out.println("descriptor enabled");
            } else {
                System.out.println("Descriptor null");
            }

//            BluetoothGattDescriptor descriptor =
//                    characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
//
//            descriptor.setValue(
//                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(descriptor);


//            String receiverSn = "41QJ4Q000000";
//            byte[] bondkey = (receiverSn).getBytes(StandardCharsets.US_ASCII);
//
//            BluetoothGattService cgmService = gatt.getService(CGMService);
//            BluetoothGattCharacteristic mAuthenticationCharacteristic = cgmService.getCharacteristic(Authentication);
//
//            if (mAuthenticationCharacteristic.setValue(bondkey)) {
//                System.out.println("STATE: onServicesDiscovered - going to write characteristic");
//                gatt.writeCharacteristic(mAuthenticationCharacteristic);
//            }

//
//            try {
//                BluetoothGattService cgmService = gatt.getService(CGMService);
//                if (cgmService != null) {
//                    System.out.println("CGM service not null");
////                BluetoothGattCharacteristic authCharacteristic = cgmService.getCharacteristic(Authentication);
//                    BluetoothGattCharacteristic controlCharacteristic = cgmService.getCharacteristic(Control);
////                BluetoothGattCharacteristic communicationCharacteristic = cgmService.getCharacteristic(Communication);
//                    gatt.setCharacteristicNotification(controlCharacteristic, true);
//                    System.out.println("CHARActeristic notification set");
//
//                    final BluetoothGattDescriptor descriptor = controlCharacteristic.getDescriptor(CharacteristicUpdateNotification);
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
//
//                    // new style
//                    GlucoseTxMessage glucoseTxMessage = new GlucoseTxMessage();
//                    controlCharacteristic.setValue(glucoseTxMessage.byteSequence);
//                    System.out.println("OKKK");
//
////                    // old style
////                    SensorTxMessage sensorTx = new SensorTxMessage();
////                    controlCharacteristic.setValue(sensorTx.byteSequence);
//
//                    gatt.writeDescriptor(descriptor);
//                    System.out.println("write descripotr called from discovered services");
//                }
//            } catch (NullPointerException e) {
//                System.out.println("NULL POINTER");
//            }
        }

//            BluetoothGattCharacteristic characteristic =
//                    gatt.getService(Control)
//                            .getCharacteristic(CharacteristicE);
//
//            BluetoothGattDescriptor descriptor =
//                    characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
//
//            descriptor.setValue(
//                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(descriptor);

//            RxBleLog.d("onServicesDiscovered status=%d", status);
//            nativeCallbackDispatcher.notifyNativeServicesDiscoveredCallback(gatt, status);
//            super.onServicesDiscovered(gatt, status);
//
//            if (servicesDiscoveredOutput.hasObservers()
//                    && !propagateErrorIfOccurred(servicesDiscoveredOutput, gatt, status, BleGattOperationType.SERVICE_DISCOVERY)) {
//                servicesDiscoveredOutput.valueRelay.call(new RxBleDeviceServices(gatt.getServices()));
//            }
//    }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            counter++;
            System.out.println("STATE: onCharacteristicRead");


            BluetoothGattService cgmService = gatt.getService(CGMService);
            BluetoothGattCharacteristic controlCharacteristic = cgmService.getCharacteristic(Control);

            gatt.setCharacteristicNotification(controlCharacteristic, true);
            final BluetoothGattDescriptor descriptor = controlCharacteristic.getDescriptor(CharacteristicUpdateNotification);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            GlucoseTxMessage glucoseTxMessage = new GlucoseTxMessage();
            controlCharacteristic.setValue(glucoseTxMessage.byteSequence);

            gatt.writeCharacteristic(controlCharacteristic);
//            gatt.writeDescriptor(descriptor);
            System.out.println("write descriptor from char read");

//            RxBleLog.d("onCharacteristicRead characteristic=%s status=%d", characteristic.getUuid(), status);
//            nativeCallbackDispatcher.notifyNativeReadCallback(gatt, characteristic, status);
//            super.onCharacteristicRead(gatt, characteristic, status);
//
//            if (readCharacteristicOutput.hasObservers() && !propagateErrorIfOccurred(
//                    readCharacteristicOutput, gatt, characteristic, status, BleGattOperationType.CHARACTERISTIC_READ
//            )) {
//                readCharacteristicOutput.valueRelay.call(new ByteAssociation<>(characteristic.getUuid(), characteristic.getValue()));
//            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("STATE: onCharacteristicWrite");

            if (counter == 0) {
//                gatt.readCharacteristic(characteristic);
            }
//            RxBleLog.d("onCharacteristicWrite characteristic=%s status=%d", characteristic.getUuid(), status);
//            nativeCallbackDispatcher.notifyNativeWriteCallback(gatt, characteristic, status);
//            super.onCharacteristicWrite(gatt, characteristic, status);
//
//            if (writeCharacteristicOutput.hasObservers() && !propagateErrorIfOccurred(
//                    writeCharacteristicOutput, gatt, characteristic, status, BleGattOperationType.CHARACTERISTIC_WRITE
//            )) {
//                writeCharacteristicOutput.valueRelay.call(new ByteAssociation<>(characteristic.getUuid(), characteristic.getValue()));
//            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            // bluetoothglucosemeter / line 669
            System.out.println("STATE: onCharacteristicChanged");
            String data = characteristic.getValue().toString();
            List<BluetoothGattService> services = gatt.getServices();
            System.out.println("onCharacteristicChanged:SERVICES : " + services.toString());
            System.out.println("onCharacteristicChanged:SERVICES SIZE: " + services.size());

            System.out.println("received data " + data);

//            RxBleLog.d("onCharacteristicChanged characteristic=%s", characteristic.getUuid());
//            nativeCallbackDispatcher.notifyNativeChangedCallback(gatt, characteristic);
//            super.onCharacteristicChanged(gatt, characteristic);
//
//            /*
//             * It is important to call changedCharacteristicSerializedPublishRelay as soon as possible because a quick changing
//             * characteristic could lead to out-of-order execution since onCharacteristicChanged may be called on arbitrary
//             * threads.
//             */
//            if (changedCharacteristicSerializedPublishRelay.hasObservers()) {
//                changedCharacteristicSerializedPublishRelay.call(
//                        new CharacteristicChangedEvent(
//                                characteristic.getUuid(),
//                                characteristic.getInstanceId(),
//                                characteristic.getValue()
//                        )
//                );
//            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            System.out.println("STATE: onDescRead");
//            RxBleLog.d("onCharacteristicRead descriptor=%s status=%d", descriptor.getUuid(), status);
//            nativeCallbackDispatcher.notifyNativeDescriptorReadCallback(gatt, descriptor, status);
//            super.onDescriptorRead(gatt, descriptor, status);
//
//            if (readDescriptorOutput.hasObservers()
//                    && !propagateErrorIfOccurred(readDescriptorOutput, gatt, descriptor, status, BleGattOperationType.DESCRIPTOR_READ)) {
//                readDescriptorOutput.valueRelay.call(new ByteAssociation<>(descriptor, descriptor.getValue()));
//            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            System.out.println("STATE: onDescriptorWrite");

//            if (counter == 0) {
//                System.out.println("STATE: onDescriptorWriteAUth");
//                String receiverSn = "41QJ4Q000000";
//                byte[] bondkey = (receiverSn).getBytes(StandardCharsets.US_ASCII);
//
//                BluetoothGattService cgmService = gatt.getService(CGMService);
//                BluetoothGattCharacteristic mAuthenticationCharacteristic = cgmService.getCharacteristic(Authentication);
//
//                if (mAuthenticationCharacteristic.setValue(bondkey)) {
//                    System.out.println("STATE: onServicesDiscovered - going to write characteristic");
//                    gatt.writeCharacteristic(mAuthenticationCharacteristic);
//                }
//
//                List<BluetoothGattService> services = gatt.getServices();
//                System.out.println("SERVICES SIZE after auth: " + services.size());
//            } else {
            System.out.println("STATE: onDescriptorWriteTxGlucose");
            BluetoothGattService cgmService = gatt.getService(CGMService);
            BluetoothGattCharacteristic controlCharacteristic = cgmService.getCharacteristic(Control);

            gatt.setCharacteristicNotification(controlCharacteristic, true);

            List<BluetoothGattDescriptor> descriptors = controlCharacteristic.getDescriptors();
            BluetoothGattDescriptor desc =
                    controlCharacteristic.getDescriptor(descriptors.get(0).getUuid());

            desc.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            GlucoseTxMessage glucoseTxMessage = new GlucoseTxMessage();
            controlCharacteristic.setValue(glucoseTxMessage.byteSequence);

            gatt.writeCharacteristic(desc.getCharacteristic());
            System.out.println("onDescriptorWritePerformed");
//            }
/*
            // BluetoothGlucoseMeter - line 227 -> GLUCOSE_SERVICE, RECORDS_CHARACTERISTIC, "readings indication request"
            System.out.println("STATE: onDescriptorWrite");

            BluetoothGattCharacteristic characteristic =
                    gatt.getService(CGMService)
                            .getCharacteristic(Control);
//            characteristic.setValue(new byte[]{1, 102, 97, 101, 97, 52, 55, 100, 56, 2});
            characteristic.setValue(new GlucoseTxMessage().byteSequence);
            gatt.writeCharacteristic(characteristic);
            System.out.println("STATE: onDescriptorWrite characteristic write");
*/

//            BluetoothGattService cgm = gatt.getService(CGMService);
//            if (cgm != null) {
//                gatt.writeCharacteristic(cgm.getCharacteristic(Authentication));
//                System.out.println("auth write characteristic ok");
//            }
//
//            System.out.println("discover services called again");
//            gatt.discoverServices();

//            RxBleLog.d("onDescriptorWrite descriptor=%s status=%d", descriptor.getUuid(), status);
//            nativeCallbackDispatcher.notifyNativeDescriptorWriteCallback(gatt, descriptor, status);
//            super.onDescriptorWrite(gatt, descriptor, status);
//
//            if (writeDescriptorOutput.hasObservers()
//                    && !propagateErrorIfOccurred(writeDescriptorOutput, gatt, descriptor, status, BleGattOperationType.DESCRIPTOR_WRITE)) {
//                writeDescriptorOutput.valueRelay.call(new ByteAssociation<>(descriptor, descriptor.getValue()));
//            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
//            RxBleLog.d("onReliableWriteCompleted status=%d", status);
//            nativeCallbackDispatcher.notifyNativeReliableWriteCallback(gatt, status);
//            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            RxBleLog.d("onReadRemoteRssi rssi=%d status=%d", rssi, status);
//            nativeCallbackDispatcher.notifyNativeReadRssiCallback(gatt, rssi, status);
//            super.onReadRemoteRssi(gatt, rssi, status);
//
//            if (readRssiOutput.hasObservers()
//                    && !propagateErrorIfOccurred(readRssiOutput, gatt, status, BleGattOperationType.READ_RSSI)) {
//                readRssiOutput.valueRelay.call(rssi);
//            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
//            RxBleLog.d("onMtuChanged mtu=%d status=%d", mtu, status);
//            nativeCallbackDispatcher.notifyNativeMtuChangedCallback(gatt, mtu, status);
//            super.onMtuChanged(gatt, mtu, status);
//
//            if (changedMtuOutput.hasObservers()
//                    && !propagateErrorIfOccurred(changedMtuOutput, gatt, status, BleGattOperationType.ON_MTU_CHANGED)) {
//                changedMtuOutput.valueRelay.call(mtu);
//            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void getDataThermo() {


        try {
            BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
            if (!bluetooth.isEnabled()) {
                bluetooth.enable();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (device.getName().contains("Dexcom")) {

                    ParcelUuid[] uuids = device.getUuids();
                    Toast.makeText(getBaseContext(), "UUIDs : " + Arrays.toString(uuids), Toast.LENGTH_SHORT).show();
                    System.out.println("UUIDS : " + Arrays.toString(uuids));

                    Toast.makeText(getBaseContext(), "Device " + deviceName + " is going to be connected", Toast.LENGTH_SHORT).show();
//                    ConnectedThread thread = new ConnectedThread(this, device,this);
//                    thread.execute();
                    device.connectGatt(getApplicationContext(), true, bluetoothGattCallback);

                    try {
                        Thread.sleep(60000 * 7);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        Object iBluetoothGatt = getIBluetoothGatt(getIBluetoothManager());
                        if (iBluetoothGatt == null) {

                        }

                        BluetoothGatt bluetoothGatt = createBluetoothGatt(iBluetoothGatt, device);

                        if (bluetoothGatt == null) {

                        }

                        boolean connectedSuccessfully = connectUsingReflection(bluetoothGatt, bluetoothGattCallback, true);

                        if (!connectedSuccessfully) {

                        }


                    } catch (NoSuchMethodException
                            | IllegalAccessException
                            | IllegalArgumentException
                            | InvocationTargetException
                            | InstantiationException
                            | NoSuchFieldException exception) {
//                        RxBleLog.w(exception, "Error during reflection");
//                        return connectGattCompat(bluetoothGattCallback, remoteDevice, true);

                    }


                }

            }
        } else {
            Toast.makeText(getBaseContext(), "No devices paired", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getBaseContext(), "End", Toast.LENGTH_SHORT).show();

    }


    private Object getIBluetoothManager() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            return null;
        }

        Method getBluetoothManagerMethod = getMethodFromClass(bluetoothAdapter.getClass(), "getBluetoothManager");
        return getBluetoothManagerMethod.invoke(bluetoothAdapter);
    }

    private Object getIBluetoothGatt(Object iBluetoothManager)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (iBluetoothManager == null) {
            return null;
        }

        Method getBluetoothGattMethod = getMethodFromClass(iBluetoothManager.getClass(), "getBluetoothGatt");
        return getBluetoothGattMethod.invoke(iBluetoothManager);
    }

    private Method getMethodFromClass(Class<?> cls, String methodName) throws NoSuchMethodException {
        Method method = cls.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
    }

    private boolean connectUsingReflection(BluetoothGatt bluetoothGatt, BluetoothGattCallback bluetoothGattCallback, boolean autoConnect)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
//        RxBleLog.v("Connecting using reflection");
        setAutoConnectValue(bluetoothGatt, autoConnect);
        Method connectMethod = bluetoothGatt.getClass().getDeclaredMethod("connect", Boolean.class, BluetoothGattCallback.class);
        connectMethod.setAccessible(true);
        return (Boolean) (connectMethod.invoke(bluetoothGatt, true, bluetoothGattCallback));
    }

    private void setAutoConnectValue(BluetoothGatt bluetoothGatt, boolean autoConnect) throws NoSuchFieldException, IllegalAccessException {
        Field autoConnectField = bluetoothGatt.getClass().getDeclaredField("mAutoConnect");
        autoConnectField.setAccessible(true);
        autoConnectField.setBoolean(bluetoothGatt, autoConnect);
    }

    private BluetoothGatt createBluetoothGatt(Object iBluetoothGatt, BluetoothDevice remoteDevice)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor bluetoothGattConstructor = BluetoothGatt.class.getDeclaredConstructors()[0];
        bluetoothGattConstructor.setAccessible(true);
//        RxBleLog.v("Found constructor with args count = " + bluetoothGattConstructor.getParameterTypes().length);

        if (bluetoothGattConstructor.getParameterTypes().length == 4) {
            return (BluetoothGatt) (bluetoothGattConstructor.newInstance(getApplicationContext(), iBluetoothGatt, remoteDevice, TRANSPORT_LE));
        } else {
            return (BluetoothGatt) (bluetoothGattConstructor.newInstance(getApplicationContext(), iBluetoothGatt, remoteDevice));
        }
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


