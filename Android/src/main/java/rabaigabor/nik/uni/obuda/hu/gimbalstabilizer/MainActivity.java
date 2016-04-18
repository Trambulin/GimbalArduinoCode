package rabaigabor.nik.uni.obuda.hu.gimbalstabilizer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter mBluetoothAdapter;
    IntentFilter filter;
    ArrayList<BluetoothDevice> foundDevices;
    Button scanbt;
    ListView listv;
    ArrayAdapter<String> listAdapt;

    TextView tv;
    int cc=0;
    boolean ff=false;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if(!((BConnection)getApplicationContext()).isConnected){
                timerHandler.postDelayed(this, 55);
            }
            else {
                Intent controlActivity=new Intent(MainActivity.this,GimbalControlActivity.class);
                startActivity(controlActivity);
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundDevices.add(device);
                // Add the name and address to an array adapter to show in a ListView
                listAdapt.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv=(TextView)findViewById(R.id.textView);

        scanbt = (Button) findViewById(R.id.button);
        listv = (ListView) findViewById(R.id.listView);
        listv.setOnItemClickListener(this);
        listAdapt = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, 0);
        listv.setAdapter(listAdapt);
        foundDevices=new ArrayList<BluetoothDevice>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        scanbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent controlActivity=new Intent(MainActivity.this,GimbalControlActivity.class);
//                startActivity(controlActivity);
                if (mBluetoothAdapter.isDiscovering()) {
                    listAdapt.add("discov folyamatban van");
                } else {
                    if (mBluetoothAdapter.startDiscovery()) {
                        listAdapt.add("discov kezdődik");
                    } else {
                        listAdapt.add("nem sikerült discoválni");
                    }
                }
            }
        });
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        BluetoothDevice selectedDevice = null;
        for (BluetoothDevice dev : foundDevices) {
            if ((dev.getName()+"\n"+dev.getAddress()).equals(listAdapt.getItem(arg2))) {
                selectedDevice = dev;
            }
        }
        if(selectedDevice!=null){
            ((BConnection)this.getApplicationContext()).initConnect(selectedDevice,mBluetoothAdapter);
            Toast.makeText(getApplicationContext(), "Waiting for connection...", Toast.LENGTH_SHORT).show();
            timerHandler.postDelayed(timerRunnable, 55);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
//        clientT.start();
//        sensorUses.sensorResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        timerHandler.removeCallbacks(timerRunnable);
//        clientT.cancel();
//        sensorUses.sensorPause();
    }
}
