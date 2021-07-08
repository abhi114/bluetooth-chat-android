package com.example.bluetooth_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //to provide functionality like to swithc on/off the bluetooth, list of all the paired devices
    private BluetoothAdapter bluetoothAdapter;
    private final int LOCATION_PERMISSION_REQUEST = 101;

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

    //devices running marsh-mellow and above needs dynamic permission request
    //Class for retrieving various kinds of information related to the application packages that are currently installed on the device.
    private void checkPermissions(){
        //Determine whether you have been granted a particular permission.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //if the permission is not granted
            //Helper for accessing features in Activity.
            //Requests permissions to be granted to this application.
            // String: The requested permissions. Must me non-null and not empty.
            //int: Application specific request code to match with a result reported to ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult
            // These permissions must be requested in your manifest
            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            //int: The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED
            // or PackageManager.PERMISSION_DENIED. Never null.
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //if the permission is granted


            }else{
                //if permission is denied
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("LOCATION PERMISSION IS REQUIRED")
                        //Interface used to allow the creator of a dialog to run some code when an item on the dialog is clicked.
                        .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkPermissions();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                }).create();
            }
        }else {
            //if it is some other request
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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