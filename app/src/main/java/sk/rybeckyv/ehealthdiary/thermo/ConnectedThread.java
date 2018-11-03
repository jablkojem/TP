package sk.rybeckyv.ehealthdiary.thermo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

class ConnectedThread extends AsyncTask<Void, Double, Void>{

    private String TAG = "M_LOG";
    private static final UUID STANDARD_SPP_UUID = UUID.fromString("F8083534-849E-531C-C594-30F1F86A4EA5");
    private MyInterface myInterface;
    private BluetoothDevice device;
    private BluetoothSocket mmSocket;

//    byte time[] =          {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,       0x00, 0x00};
//    byte readData[] =      {0x51, 0x26, 0x00, 0x00, 0x00, 0x00, (byte)0xA3, 0x1A};
//    byte readTime[] =      {0x51, 0x25, 0x00, 0x00, 0x00, 0x00, (byte)0xA3, 0x19};
    byte receivedData[] =  {0x00, 0x00, 0x00, 0x00, 0x00, 0x00,       0x00, 0x00};

    public ConnectedThread(MyInterface myInterface, BluetoothDevice device) {
        this.myInterface = myInterface;
        this.device = device;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();


        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket = this.device.createInsecureRfcommSocketToServiceRecord(STANDARD_SPP_UUID);
            mmSocket.connect();

            OutputStream mmOutStream = mmSocket.getOutputStream();
            InputStream mmInStream = mmSocket.getInputStream();

            mmOutStream.write(new GlucoseTxMessage().byteSequence);
            Thread.sleep(300);

            mmInStream.read(receivedData);

            final double temperature = ((256 * (int) receivedData[3] + (int) receivedData[2]) * 0.1);
            publishProgress(temperature);

        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        myInterface.onThermoUpdated(values[0]);
    }
}