package com.example.bluetooth_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    //paired devices list
    private ListView listPairedDevices,listAvailableDevices;

    //adapter for our list items
    private ArrayAdapter<String> adapterPairedDevices,adapterAvailableDevices;

    //bluetooth adapter can be used to get the list of paired devices
    private BluetoothAdapter bluetoothAdapter;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        init();
    }

    //initialize all the list
    private void init(){
        listPairedDevices = findViewById(R.id.list_paired_devices);
        listAvailableDevices = findViewById(R.id.list_available_devices);
        //a layout file to structure each item
        adapterPairedDevices = new ArrayAdapter<String>(this,R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(this,R.layout.device_list_item);

        progressBar = findViewById(R.id.progress_scan_devices);

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //get all the paired devices
        //Return the set of BluetoothDevice objects that are bonded (paired) to the local adapter.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        //on click listener for our list views
        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //we are getting the name and the address that we have shown to the user
                //it is a view so we have to type cast it to textView first
                String info = ((TextView)view).getText().toString();
                //the address is of 17 letters
                String address = info.substring(info.length() -17);

                Intent intent = new Intent();
                intent.putExtra("deviceAddress",address);
                //Call this to set the result that your activity will return to its caller.
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        if(pairedDevices!=null && pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                //fill the adapter
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }
        // Register for broadcasts when a device is discovered.
        //Structured description of Intent values to be matched. An IntentFilter can match against actions,
        IntentFilter  intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND); //Broadcast Action: Remote device discovered.
        //Register a BroadcastReceiver to be run in the main activity thread.
        // The receiver will be called with any broadcast Intent that matches filter, in the main application thread.
        registerReceiver(bluetoothDeviceListener,intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // Broadcast Action: The local Bluetooth adapter has finished the device discovery process.
        registerReceiver(bluetoothDeviceListener,intentFilter1);
    }
    //To receive information about each device discovered,
    // your app must register a BroadcastReceiver for the ACTION_FOUND intent.
    //action found -- Broadcast Action: Remote device discovered.
    //Sent when a remote device is found during discovery.
    private BroadcastReceiver bluetoothDeviceListener= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Retrieve the general action to be performed, such as ACTION_VIEW.
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //The intent contains the extra fields EXTRA_DEVICE and EXTRA_CLASS,
                // which in turn contain a BluetoothDevice and a BluetoothClass
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //if its not already connected
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
                //Broadcast Action: The local Bluetooth adapter has finished the device discovery process.
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                progressBar.setVisibility(View.GONE);
                //if no devices has been found
                if(adapterAvailableDevices.getCount() == 0){
                    Toast.makeText(DeviceListActivity.this,"No new devices found",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(DeviceListActivity.this,"click on the device to start the chat",Toast.LENGTH_SHORT).show();

                }
            }

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_device_list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_scan_devices:
                //Toast.makeText(this,"Scan Devices",Toast.LENGTH_SHORT).show();
                scanDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //scan for available devices
    private void scanDevices(){
        progressBar.setVisibility(View.VISIBLE);
        //we have to hide all the already present available devices and inflate the current devices
        adapterAvailableDevices.clear();

        Toast.makeText(this,"Scanning",Toast.LENGTH_SHORT).show();
        //Return true if the local Bluetooth adapter is currently in the device discovery process.
        if(bluetoothAdapter.isDiscovering()){
            //Cancel the current device discovery process.
            bluetoothAdapter.cancelDiscovery();
        }
        //The discovery process usually involves an inquiry scan of about 12 seconds,
        // followed by a page scan of each new device to retrieve its Bluetooth name.
        bluetoothAdapter.startDiscovery();
        //Register for BluetoothDevice.ACTION_FOUND to be notified as remote Bluetooth devices are found.
    }

}