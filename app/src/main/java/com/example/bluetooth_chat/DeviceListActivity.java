package com.example.bluetooth_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    //paired devices list
    private ListView listPairedDevices,listAvailableDevices;

    //adapter for our list items
    private ArrayAdapter<String> adapterPairedDevices,adapterAvailableDevices;

    //bluetooth adapter can be used to get the list of paired devices
    private BluetoothAdapter bluetoothAdapter;

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

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //get all the paired devices
        //Return the set of BluetoothDevice objects that are bonded (paired) to the local adapter.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if(pairedDevices!=null && pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                //fill the adapter
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

}