package sk.rybeckyv.ehealthdiary.thermo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

class ConnectedThread extends AsyncTask<Void, Double, Void> {

    private String TAG = "M_LOG";
    private static final UUID STANDARD_SPP_UUID = UUID.fromString("F8083534-849E-531C-C594-30F1F86A4EA5");
    public static final UUID Authentication = UUID.fromString("F8083535-849E-531C-C594-30F1F86A4EA5");
    static final UUID CGM_CHARACTERISTIC_INDICATE = UUID.fromString("669A9101-0008-968F-E311-6050405558B3");
    UUID DEFAULT_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final UUID Communication = UUID.fromString("F8083533-849E-531C-C594-30F1F86A4EA5");
    private static final UUID GLUCOSE_SERVICE = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");

    private MyInterface myInterface;
    private BluetoothDevice device;
    private BluetoothSocket mmSocket;
    private Activity activity;

    //    byte time[] =          {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,       0x00, 0x00};
//    byte readData[] =      {0x51, 0x26, 0x00, 0x00, 0x00, 0x00, (byte)0xA3, 0x1A};
//    byte readTime[] =      {0x51, 0x25, 0x00, 0x00, 0x00, 0x00, (byte)0xA3, 0x19};
    byte receivedData[] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public ConnectedThread(MyInterface myInterface, BluetoothDevice device, Activity activity) {
        this.myInterface = myInterface;
        this.device = device;
        this.activity = activity;
    }


    @Override
    protected Void doInBackground(Void... voids) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();


        try {

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Thread started", Toast.LENGTH_SHORT).show();
                }
            });


            mmSocket = this.device.createInsecureRfcommSocketToServiceRecord(GLUCOSE_SERVICE);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "will be connected", Toast.LENGTH_SHORT).show();
                }
            });
            mmSocket.connect();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "is connected", Toast.LENGTH_SHORT).show();
                }
            });

            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket = this.device.createInsecureRfcommSocketToServiceRecord(STANDARD_SPP_UUID);
            mmSocket.connect();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Device connected", Toast.LENGTH_SHORT).show();
                }
            });
            OutputStream mmOutStream = mmSocket.getOutputStream();
            InputStream mmInStream = mmSocket.getInputStream();

            mmOutStream.write(new GlucoseTxMessage().byteSequence);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Bytes written", Toast.LENGTH_SHORT).show();
                }
            });
            Thread.sleep(5000);

            mmInStream.read(receivedData);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Received data : " + receivedData.toString(), Toast.LENGTH_SHORT).show();
                }
            });

            ByteBuffer data = ByteBuffer.wrap(receivedData).order(ByteOrder.LITTLE_ENDIAN);

            int status_raw = data.get(1);
            int status = data.get(1);
            int sequence = data.getInt(2);
            int timestamp = data.getInt(6);
            int glucoseBytes = data.getShort(10); // check signed vs unsigned!!
            int glucose = glucoseBytes & 0xfff;

            int state = data.get(12);
            int trend = data.get(13);

            int unfiltered;
            int filtered = 0;

            if (glucose > 13) {
                unfiltered = glucose * 1000;
                filtered = glucose * 1000;
            } else {
                filtered = glucose;
                unfiltered = glucose;
            }
//            publishProgress((double)filtered);
            onProgressUpdate((double) filtered);

        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "IOexception", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "InterruptedException", Toast.LENGTH_SHORT).show();
                }
            });
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        myInterface.onThermoUpdated(values[0]);
    }
}