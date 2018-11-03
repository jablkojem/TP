package sk.rybeckyv.ehealthdiary.thermo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyInterface {

    private final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void getDataThermo(){

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

//                if(device.getName().startsWith("")){

                    ConnectedThread thread = new ConnectedThread(this, device);
                    thread.execute();

//                }

            }
        }

    }

    public void runMeasure(View view){
        Button button = (Button)view;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            Toast.makeText(getBaseContext(), "Device not support bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }else{

            getDataThermo();

        }
    }

    public void send(View view){
        Button button = (Button)view;


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case REQUEST_ENABLE_BT:
                Toast.makeText(getBaseContext(), "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                getDataThermo();
                break;
        }
    }

    @Override
    public void onThermoUpdated(double thermo) {
        TextView textView = (TextView)findViewById(R.id.value);
        textView.setText(getString(R.string.thermo_value, thermo));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, String> params = new HashMap<>();
        params.put("id", UUID.randomUUID().toString());
        params.put("table", "TEPLOMER");
        params.put("teplota", String.format("%.2f", thermo));
        params.put("datum", sdf.format(new Date()));

        JSONObject object = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://147.175.98.38/timak2/api/add_value_t.php", object,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if(response.has("success")){
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
