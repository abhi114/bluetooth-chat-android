package com.example.bluetooth_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //to provide functionality like to swithc on/off the bluetooth, list of all the paired devices
    private BluetoothAdapter bluetoothAdapter;
    private final int LOCATION_PERMISSION_REQUEST = 101;
    private final int SELECT_DEVICE = 102; //request code


    //instance of the chat utility
    private ChatUtils chatUtils;

    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    public static final String DEVICE_NAME = "deviceName" ; //key to get the device name
    public static final String TOAST = "toast"; //key
    private String connectedDevice;

    //Callback interface you can use when
    // instantiating a Handler to avoid having to implement your own subclass of Handler.
    //Now, Handler provides a way to communicate with Looper. Handler sends Runnable/Message object on Looper,
    // providing a way to execute code on a particular thread from another thread.
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        //Subclasses must implement this to receive messages.
        //message - Defines a message containing a description and arbitrary data object that can be sent to a Handler.
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){//User-defined message code so that the recipient can identify what this message is about.
                case MESSAGE_STATE_CHANGED:
                    //state changed send by the chatUtils
                    switch (msg.arg1){
                        case ChatUtils.STATE_NONE:
                            setState("Not Connected");
                            break;
                        case ChatUtils.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case ChatUtils.STATE_CONNECTING:
                            setState("Connecting....");
                            break;
                        case ChatUtils.STATE_CONNECTED:
                            setState("Connected" + connectedDevice);
                            break;

                    }
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_DEVICE_NAME:
                    //Obtains a Bundle of arbitrary data associated with this event
                    connectedDevice = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(MainActivity.this, connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;

            }
            return false;
        }
    });

    //to reflect the states
    private void setState(CharSequence subTitle){
        //we are showing a subtitle in our action bar according to the state
        getSupportActionBar().setSubtitle(subTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBluetooth();
        chatUtils = new ChatUtils(MainActivity.this,handler);
    }

    //initialize the bluetooth adapter
    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //it means the device dose not have an bluetooth adapter
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
                checkPermissions();
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
        }else{
            //if the permission is granted
            Intent intent   = new Intent(MainActivity.this,DeviceListActivity.class);
            //to get back the result of an the selected device
            startActivityForResult(intent,SELECT_DEVICE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            //int: The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED
            // or PackageManager.PERMISSION_DENIED. Never null.
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //if the permission is granted
                Intent intent = new Intent(this,DeviceListActivity.class);
                startActivityForResult(intent,SELECT_DEVICE);


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
                }).show();
            }
        }else {
            //if it is some other request
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //the data returned from the devicelist activity will be managed here


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        if(requestCode == SELECT_DEVICE && resultCode == RESULT_OK){
            //it will get the address of the device which is selected
            String address = data.getStringExtra("deviceAddress");
            //and this will get the bluetooth deivce object of the device which is selected to get to connection
            //Get a BluetoothDevice object for the given Bluetooth hardware address.
            chatUtils.connect(bluetoothAdapter.getRemoteDevice(address));
            Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //to enable bluetooth
    private void enableBluetooth() {
        if(!bluetoothAdapter.isEnabled()){
            //Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
            bluetoothAdapter.enable();
        }
        //if our device is not already visible to other devices we will make it visible
        //The Bluetooth scan mode determines if the local adapter is connectable and/or discoverable from remote Bluetooth devices.
        //indicates that both inquiry scan and page scan are enabled on the local Bluetooth adapter.
        // Therefore this device is both discoverable and connectable from remote Bluetooth devices.
        if(bluetoothAdapter.getScanMode() != bluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            //This issues a request to enable the system's discoverable mode without having to navigate to the Settings app,
            // which would stop your own app.
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //give extra time for
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoveryIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatUtils!=null){
            chatUtils.stop();
        }
    }
}