package com.example.bluetooth_chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeviceListActivity extends AppCompatActivity {
    //paired devices list
    private ListView listPairedDevices,listAvailableDevices;

    //adapter for our list items
    private ArrayAdapter<String> adapterPairedDevices,adapterAvailableDevices;

    //bluetooth adapter can be used to get the list of paired devices

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
    }

}