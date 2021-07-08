package com.example.bluetooth_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //to provide functionality like to swithc on/off the bluetooth, list of all the paired devices
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBluetooth();
    }

    //initialize the bluetooth adapter
    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //it means the device dosent have an bluetooth adapter
        if(bluetoothAdapter == null){
            Toast.makeText(this,"no bluetooth found",Toast.LENGTH_SHORT).show();
        }
    }

    //To specify the options menu for an activity,
    // override onCreateOptionsMenu() (fragments provide their own onCreateOptionsMenu() callback).
    // In this method, you can inflate your menu resource (defined in XML) into the Menu provided in the callback.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }
//When the user selects an item from the options menu (including action items in the app bar), the system calls your activity's
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //MenuItem: The menu item that was selected.
        //switch statement
        //getItemId - Return the identifier for this menu item. The identifier can not be changed after the menu is created.
        switch (item.getItemId()){
            case R.id.menu_search_devices:
                Toast.makeText(this,"Clicked Search Devices",Toast.LENGTH_SHORT).show();
                return true;

            case R.id.menu_enable_bluetooth:
                enableBluetooth();
                //Toast.makeText(this,"Clicked Enable Devices",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //to enable bluetooth
    private void enableBluetooth() {
        if(bluetoothAdapter.isEnabled()){
            Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
        }else{
            bluetoothAdapter.enable();
        }
    }
}